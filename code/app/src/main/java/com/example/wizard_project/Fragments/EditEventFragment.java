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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.Facility;
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
    boolean geolocationCheckbox = false;
    private FragmentEditEventBinding binding;
    private String facilityId;
    private Event displayEvent;
    private EventController eventController;
    private FacilityController facilityController;
    private User currentUser;
    private String eventLocation = "";
    private PhotoHandler photoHandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditEventBinding.inflate(inflater, container, false);
        photoHandler = new PhotoHandler(); // Initialize PhotoHandler
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize controllers
        eventController = new EventController();
        facilityController = new FacilityController();

        // Get the current user
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        NavController navController = NavHostFragment.findNavController(this);
        String userId = currentUser.getDeviceId();

        // Set up the UI elements
        initializeUI();

        // Handle image selection
        binding.eventEditImageview.setOnClickListener(v -> openImagePicker());

        // Handle date selection
        setupDatePickers();

        // Handle save button clicks
        binding.buttonSaveEvent.setOnClickListener(v -> handleSaveButtonClick(navController));

    }

    /**
     * Initializes the UI elements and populates fields if editing an existing event.
     */
    private void initializeUI() {
        displayEvent = getArguments() != null ? (Event) getArguments().getSerializable("event") : null;

        if (displayEvent == null) {
            // Create a new event
            binding.buttonSaveEvent.setText("Create Event");
            facilityController.getFacility(currentUser.getDeviceId(), facility -> {
                if (facility != null) {
                    facilityId = facility.getFacilityId();
                    eventLocation = facility.getFacility_name();
                }
            });
        } else {
            // Populate fields for editing an existing event
            facilityId = displayEvent.getFacilityId();
            binding.edittextName.setText(displayEvent.getEvent_name());
            binding.edittextEventDescription.setText(displayEvent.getEvent_description());
            binding.edittextPrice.setText(String.valueOf(displayEvent.getEvent_price()));
            binding.edittextMaxEntrants.setText(String.valueOf(displayEvent.getEvent_max_entrants()));
            binding.switchGeolocationRequired.setChecked(displayEvent.isGeolocation_requirement());

            if (displayEvent.getRegistration_open() != null) {
                selectedRegistrationOpenDate = displayEvent.getRegistration_open();
                binding.buttonRegistrationOpen.setText(new SimpleDateFormat("yyyy-MM-dd").format(selectedRegistrationOpenDate));
            }
            if (displayEvent.getRegistration_close() != null) {
                selectedRegistrationCloseDate = displayEvent.getRegistration_close();
                binding.buttonRegistrationClose.setText(new SimpleDateFormat("yyyy-MM-dd").format(selectedRegistrationCloseDate));
            }

            binding.buttonSaveEvent.setText("Save Changes");
        }
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
     * Handles the save button click, creating or updating an event.
     */
    private void handleSaveButtonClick(NavController navController) {
        String newName = binding.edittextName.getText().toString();
        String newDescription = binding.edittextEventDescription.getText().toString();
        String newPriceString = binding.edittextPrice.getText().toString();
        String newMaxEntrantsString = binding.edittextMaxEntrants.getText().toString();
        boolean geolocationRequired = binding.switchGeolocationRequired.isChecked();

        // Check if max_entrants is empty, set to Integer.MAX_VALUE if so
        int newMaxEntrants = newMaxEntrantsString.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(newMaxEntrantsString);

        if (!validateInputs(newName, newDescription, newPriceString, newMaxEntrants)) return;

        int newPrice = Integer.parseInt(newPriceString);

        // Use existing dates if no new ones are selected
        if (selectedRegistrationOpenDate == null) {
            selectedRegistrationOpenDate = displayEvent.getRegistration_open();
        }
        if (selectedRegistrationCloseDate == null) {
            selectedRegistrationCloseDate = displayEvent.getRegistration_close();
        }

        if (displayEvent == null) {
            // Create a new event
            Event newEvent = new Event(newName, newDescription, newPrice, newMaxEntrants,
                    selectedRegistrationOpenDate, selectedRegistrationCloseDate, facilityId,
                    eventLocation, geolocationRequired, "");

            eventController.createEvent(newEvent, currentUser.getDeviceId(), new EventController.createCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
                    navigateToViewEvent(navController, newEvent);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Failed to create event.", Toast.LENGTH_SHORT).show();
                    Log.e("EditEvent", "Error creating event", e);
                }
            });
        } else {
            // Update existing event
            updateExistingEvent(newName, newDescription, newPrice, newMaxEntrants, geolocationRequired, navController);
        }
    }

    /**
     * Updates an existing event with new details.
     */
    private void updateExistingEvent(String newName, String newDescription, int newPrice, int newMaxEntrants,
                                     boolean geolocationRequired, NavController navController) {
        displayEvent.setEvent_name(newName);
        displayEvent.setEvent_description(newDescription);
        displayEvent.setEvent_price(newPrice);
        displayEvent.setEvent_max_entrants(newMaxEntrants);
        displayEvent.setRegistration_open(selectedRegistrationOpenDate);
        displayEvent.setRegistration_close(selectedRegistrationCloseDate);
        displayEvent.setGeolocation_requirement(geolocationRequired);

        eventController.updateEvent(displayEvent, new EventController.updateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show();
                navigateToViewEvent(navController, displayEvent);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to update event.", Toast.LENGTH_SHORT).show();
                Log.e("EditEvent", "Error updating event", e);
            }
        });
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
                String path = "images/" + UUID.randomUUID().toString();
                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(path);

                // Upload image to Firebase Storage
                imageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            displayEvent.setPosterUri(uri.toString());
                            displayEvent.setEvent_image_path(path);

                            eventController.updateField(displayEvent, "posterUri", uri.toString());
                            eventController.updateField(displayEvent, "event_image_path", path);

                            // Update ImageView
                            Glide.with(requireContext()).load(uri).into(binding.eventEditImageview);
                            Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("EditEvent", "Image upload error", e);
                        });
            }
        }
    }

    /**
     * Navigates to the ViewEventFragment with the updated event.
     */
    private void navigateToViewEvent(NavController navController, Event event) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        navController.navigate(R.id.action_EditEventFragment_to_ViewEventFragment, bundle);
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
