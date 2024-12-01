package com.example.wizard_project.Fragments;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.PhotoHandler;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.EventController;
import com.example.wizard_project.Controllers.FacilityController;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEditEventBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * EditEventFragment allows the user to create or edit their event.
 */
public class EditEventFragment extends Fragment {
    Date selectedRegistrationOpenDate;
    Date selectedRegistrationCloseDate;
    private FragmentEditEventBinding binding;
    private Event displayEvent;
    private EventController eventController;
    private FacilityController facilityController;
    private String facilityId;
    private String eventLocation;
    private User currentUser;
    private Uri selectedImageUri = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize controllers
        eventController = new EventController();
        facilityController = new FacilityController();

        // Get the current user
        currentUser = ((MainActivity) requireActivity()).getCurrentUser();
        NavController navController = NavHostFragment.findNavController(this);

        // Set up the UI elements
        initializeUI(navController);
    }

    /**
     * Initializes the UI elements and populates fields if editing an existing event.
     */
    private void initializeUI(NavController navController) {
        displayEvent = getArguments() != null ? (Event) getArguments().getSerializable("event") : null;

        // Check if editing an existing event or creating a new one
        if (displayEvent == null) {
            setupNewEventUI();
        } else {
            populateFieldsForEditing();
        }

        setupListeners(navController);
    }

    /**
     * Sets up the UI for creating a new event.
     */
    private void setupNewEventUI() {
        // Fetch facility details
        facilityController.getFacility(currentUser.getDeviceId(), facility -> {
            if (facility != null) {
                facilityId = facility.getFacilityId();
                eventLocation = facility.getFacility_name();
            }
        });

        // Set up UI for creating a new event
        binding.buttonSaveEvent.setText("Create Event");
        binding.eventEditImageview.setImageResource(R.drawable.example_event); // Placeholder
        binding.buttonDeleteEventImage.setVisibility(View.GONE);
        binding.eventEditImageview.setClickable(true); // Allow image picker
    }

    /**
     * Populates the fields with the existing event's data.
     */
    private void populateFieldsForEditing() {
        facilityId = displayEvent.getFacilityId();
        eventLocation = displayEvent.getEvent_location();

        binding.edittextName.setText(displayEvent.getEvent_name());
        binding.edittextEventDescription.setText(displayEvent.getEvent_description());
        binding.edittextPrice.setText(String.valueOf(displayEvent.getEvent_price()));

        // Set max entrants field
        if (displayEvent.getEvent_max_entrants() == Integer.MAX_VALUE) {
            binding.edittextMaxEntrants.setText(""); // Leave blank if there is no entrant limit
        } else {
            binding.edittextMaxEntrants.setText(String.valueOf(displayEvent.getEvent_max_entrants()));
        }

        binding.switchGeolocationRequired.setChecked(displayEvent.isGeolocation_requirement());

        // Set registration dates
        if (displayEvent.getRegistration_open() != null) {
            selectedRegistrationOpenDate = displayEvent.getRegistration_open();
            binding.buttonRegistrationOpen.setText(new SimpleDateFormat("yyyy-MM-dd").format(selectedRegistrationOpenDate));
        }
        if (displayEvent.getRegistration_close() != null) {
            selectedRegistrationCloseDate = displayEvent.getRegistration_close();
            binding.buttonRegistrationClose.setText(new SimpleDateFormat("yyyy-MM-dd").format(selectedRegistrationCloseDate));
        }

        // Set event image
        if (displayEvent.getPosterUri() != null && !displayEvent.getPosterUri().isEmpty()) {
            // Load existing event image
            Glide.with(requireContext()).load(displayEvent.getPosterUri()).into(binding.eventEditImageview);
            binding.buttonDeleteEventImage.setVisibility(View.VISIBLE);
            binding.eventEditImageview.setClickable(false); // Prevent image picker
        } else {
            // Show placeholder if no image is available
            binding.eventEditImageview.setImageResource(R.drawable.example_event); // Placeholder image
            binding.buttonDeleteEventImage.setVisibility(View.GONE);
            binding.eventEditImageview.setClickable(true); // Allow image picker
        }
    }

    /**
     * Sets up event listeners.
     *
     * @param navController The NavController for navigation.
     */
    private void setupListeners(NavController navController) {
        // Allow users to upload an event image only if there is no existing event image
        binding.eventEditImageview.setOnClickListener(v -> {
            if (displayEvent != null && displayEvent.getPosterUri() != null && !displayEvent.getPosterUri().isEmpty()) {
                Toast.makeText(requireContext(), "Please delete the current image before uploading a new one.", Toast.LENGTH_SHORT).show();
            } else {
                openImagePicker();
            }
        });

        setupDatePickers(); // Date selection buttons
        binding.buttonDeleteEventImage.setOnClickListener(v -> deleteImage()); // Delete image button
        binding.buttonSaveEvent.setOnClickListener(v -> handleSaveButtonClick(navController)); // Save button
    }

    /**
     * Sets up the date pickers for registration open and close dates.
     */
    private void setupDatePickers() {
        binding.buttonRegistrationOpen.setOnClickListener(v -> showDatePicker((date) -> {
            selectedRegistrationOpenDate = date;
            binding.buttonRegistrationOpen.setText(new SimpleDateFormat("yyyy-MM-dd").format(date));
        }));
        binding.buttonRegistrationClose.setOnClickListener(v -> showDatePicker((date) -> {
            selectedRegistrationCloseDate = date;
            binding.buttonRegistrationClose.setText(new SimpleDateFormat("yyyy-MM-dd").format(date));
        }));
    }

    /**
     * Opens an image picker to select an event poster image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PhotoHandler.PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the save button click for creating or updating an event.
     *
     * @param navController The NavController for navigation.
     */
    private void handleSaveButtonClick(NavController navController) {
        String newName = binding.edittextName.getText().toString();
        String newDescription = binding.edittextEventDescription.getText().toString();
        String newPriceString = binding.edittextPrice.getText().toString();
        String newMaxEntrantsString = binding.edittextMaxEntrants.getText().toString();
        boolean geolocationRequired = binding.switchGeolocationRequired.isChecked();

        int newMaxEntrants = newMaxEntrantsString.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(newMaxEntrantsString);

        if (!validateInputs(newName, newDescription, newPriceString, newMaxEntrants)) return;

        // Use existing dates if no new ones are selected
        if (selectedRegistrationOpenDate == null) {
            selectedRegistrationOpenDate = displayEvent.getRegistration_open();
        }
        if (selectedRegistrationCloseDate == null) {
            selectedRegistrationCloseDate = displayEvent.getRegistration_close();
        }

        int newPrice = Integer.parseInt(newPriceString);
        boolean isNewEvent = displayEvent == null;

        if (displayEvent == null) {
            // Create a new event
            displayEvent = new Event(
                    null, // Pass null to generate a new eventId
                    newName,
                    newDescription,
                    newPrice,
                    newMaxEntrants,
                    selectedRegistrationOpenDate,
                    selectedRegistrationCloseDate,
                    facilityId,
                    eventLocation,
                    geolocationRequired,
                    "" // Placeholder for event_image_path
            );
        } else {
            // Update existing event details
            displayEvent.setEvent_name(newName);
            displayEvent.setEvent_description(newDescription);
            displayEvent.setEvent_price(newPrice);
            displayEvent.setEvent_max_entrants(newMaxEntrants);
            displayEvent.setRegistration_open(selectedRegistrationOpenDate);
            displayEvent.setRegistration_close(selectedRegistrationCloseDate);
            displayEvent.setGeolocation_requirement(geolocationRequired);
        }

        // Upload image if selected
        if (selectedImageUri != null) {
            uploadImageToFirebase(selectedImageUri, new EventController.updateCallback() {
                @Override
                public void onSuccess() {
                    saveEventToDatabase(navController, isNewEvent); // Save event after successful upload
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Image upload failed.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveEventToDatabase(navController, isNewEvent); // Save event without uploading an image
        }
    }

    /**
     * Uploads the selected image to Firebase Storage.
     *
     * @param imageUri The URI of the selected image.
     */
    private void uploadImageToFirebase(Uri imageUri, EventController.updateCallback callback) {
        String path = "images/" + UUID.randomUUID().toString();
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(path);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    displayEvent.setPosterUri(uri.toString());
                    displayEvent.setEvent_image_path(path);
                    callback.onSuccess();
                }))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Saves the event to the database.
     *
     * @param navController The NavController for navigation.
     * @param isNewEvent    Whether the event is new or being updated.
     */
    private void saveEventToDatabase(NavController navController, boolean isNewEvent) {
        if (displayEvent == null) return;

        // If the event ID is empty, create a new event
        if (isNewEvent) {
            eventController.createEvent(displayEvent, currentUser.getDeviceId(), new EventController.createCallback() {
                @Override
                public void onSuccess() {
                    selectedImageUri = null; // Clear after save
                    navigateToViewEvent(navController, displayEvent); // Navigate to the ViewEventFragment
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Failed to create event.", Toast.LENGTH_SHORT).show();
                }
            });
            // If the event ID is not empty, update an existing event
        } else {
            eventController.updateEvent(displayEvent, new EventController.updateCallback() {
                @Override
                public void onSuccess() {
                    selectedImageUri = null; // Clear after save
                    navigateToViewEvent(navController, displayEvent); // Navigate to the ViewEventFragment
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Failed to update event.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Deletes the current event image from Firebase Storage.
     */
    private void deleteImage() {
        if (displayEvent != null && displayEvent.getEvent_image_path() != null && !displayEvent.getEvent_image_path().isEmpty()) {
            // Delete the image from Firebase Storage
            String imagePath = displayEvent.getEvent_image_path();
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);

            imageRef.delete().addOnSuccessListener(aVoid -> {
                // Clear the event image fields
                displayEvent.setPosterUri("");
                displayEvent.setEvent_image_path("");
                eventController.updateField(displayEvent, "posterUri", "");
                eventController.updateField(displayEvent, "event_image_path", "");

                // Reset the UI to show the placeholder image
                binding.eventEditImageview.setImageResource(R.drawable.example_event);
                binding.buttonDeleteEventImage.setVisibility(View.GONE);
                binding.eventEditImageview.setClickable(true); // Re-enable image picker

                Toast.makeText(requireContext(), "Image deleted successfully.", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("EditEventFragment", "Error deleting image", e);
            });
        } else {
            Toast.makeText(requireContext(), "No image to delete.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates the user inputs for creating or editing an event.
     */
    private boolean validateInputs(String name, String description, String price, int maxEntrants) {
        if (name.isEmpty()) {
            binding.edittextName.setError("Please enter a valid event name.");
            return false;
        }
        if (description.isEmpty()) {
            binding.edittextEventDescription.setError("Please enter a valid event description.");
            return false;
        }
        if (price.isEmpty() || !price.matches("\\d+")) {
            binding.edittextPrice.setError("Please enter a valid price.");
            return false;
        }
        if (maxEntrants < 0) {
            binding.edittextMaxEntrants.setError("Please enter a valid max entrants.");
            return false;
        }
        if (selectedRegistrationOpenDate == null) {
            Toast.makeText(requireContext(), "Please select a registration open date.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedRegistrationCloseDate == null) {
            Toast.makeText(requireContext(), "Please select a registration close date.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Handles image selection and uploads it to Firebase.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoHandler.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                selectedImageUri = imageUri;
                Glide.with(requireContext()).load(selectedImageUri).into(binding.eventEditImageview);
                binding.buttonDeleteEventImage.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Image selected. Save to update.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Navigates to ViewEventFragment with the updated event.
     *
     * @param navController The NavController for navigation.
     * @param event         The updated event object.
     */
    private void navigateToViewEvent(NavController navController, Event event) {
        if (navController.getCurrentDestination() != null) {
            Log.d("Navigation", "Current destination: " + navController.getCurrentDestination().getLabel());
        }

        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == R.id.EditEventFragment) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            navController.navigate(R.id.action_EditEventFragment_to_ViewEventFragment, bundle);
        } else {
            Log.e("EditEventFragment", "Invalid navigation: Current destination is not EditEventFragment.");
        }
    }

    /**
     * Displays a DatePickerDialog and returns the selected date through a callback.
     *
     * @param onDateSelected A callback that receives the selected date.
     */
    private void showDatePicker(OnDateSelectedCallback onDateSelected) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    Date selectedDate = calendar.getTime();
                    onDateSelected.onDateSelected(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    /**
     * Callback interface for returning the selected date from the DatePickerDialog.
     */
    @FunctionalInterface
    private interface OnDateSelectedCallback {
        void onDateSelected(Date date);
    }
}
