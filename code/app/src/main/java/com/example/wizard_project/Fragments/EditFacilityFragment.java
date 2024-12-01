package com.example.wizard_project.Fragments;

import static android.app.Activity.RESULT_OK;

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
import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.Classes.PhotoHandler;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.FacilityController;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEditFacilityBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

/**
 * EditFacilityFragment allows an organizer to create or edit a facility.
 */
public class EditFacilityFragment extends Fragment {
    private final FacilityController facilityController = new FacilityController();
    private FragmentEditFacilityBinding binding;
    private User currentUser;
    private Facility userFacility;
    private Uri selectedImageUri = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditFacilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current user and NavController
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        NavController navController = NavHostFragment.findNavController(this);

        // Initialize UI
        initializeUI(navController);
    }

    /**
     * Initializes the UI elements and populates them based on the existing facility (if any).
     *
     * @param navController The NavController for navigation.
     */
    private void initializeUI(NavController navController) {
        // Get facility passed to the fragment
        userFacility = (Facility) (getArguments() != null ? getArguments().getSerializable("facility") : null);

        if (userFacility == null) {
            // Set up for creating a new facility
            binding.edittextName.setText("");
            binding.edittextFacilityLocation.setText("");
            binding.imageviewEditFacilityImage.setImageResource(R.drawable.example_facility); // Placeholder image
            binding.buttonSaveFacility.setText("Create Facility");
            binding.buttonDeleteFacilityImage.setVisibility(View.GONE);
            binding.imageviewEditFacilityImage.setClickable(true); // Allow image picker
        } else {
            // Populate fields for editing existing facility
            binding.edittextName.setText(userFacility.getFacility_name());
            binding.edittextFacilityLocation.setText(userFacility.getFacility_location());
            binding.buttonSaveFacility.setText("Save Changes");

            // Load existing facility image or placeholder if not available
            if (userFacility.getposterUri() != null && !userFacility.getposterUri().isEmpty()) {
                Glide.with(requireContext()).load(userFacility.getposterUri()).into(binding.imageviewEditFacilityImage);
                binding.buttonDeleteFacilityImage.setVisibility(View.VISIBLE);
                binding.imageviewEditFacilityImage.setClickable(false); // Disable image picker
            } else {
                binding.imageviewEditFacilityImage.setImageResource(R.drawable.example_facility); // Placeholder
                binding.buttonDeleteFacilityImage.setVisibility(View.GONE);
                binding.imageviewEditFacilityImage.setClickable(true); // Allow image picker
            }
        }

        // Set up listeners
        setupListeners(navController);
    }

    /**
     * Sets up the event listeners for UI elements.
     *
     * @param navController The NavController for navigation.
     */
    private void setupListeners(NavController navController) {
        // Handle image selection
        binding.imageviewEditFacilityImage.setOnClickListener(v -> {
            if (userFacility != null && userFacility.getposterUri() != null && !userFacility.getposterUri().isEmpty()) {
                // Prevent new image selection
                Toast.makeText(requireContext(), "Please delete the current image before uploading a new one.", Toast.LENGTH_SHORT).show();
            } else {
                // Allow image selection
                openImagePicker();
            }
        });

        // Save button listener
        binding.buttonSaveFacility.setOnClickListener(v -> handleSaveButton(navController));

        // Delete image button listener
        binding.buttonDeleteFacilityImage.setOnClickListener(v -> deleteImage());

        // Show delete button if a facility image exists
        if (userFacility != null && userFacility.getposterUri() != null && !userFacility.getposterUri().isEmpty()) {
            binding.buttonDeleteFacilityImage.setVisibility(View.VISIBLE);
        } else {
            binding.buttonDeleteFacilityImage.setVisibility(View.GONE);
        }
    }

    /**
     * Opens an image picker for selecting a facility image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PhotoHandler.PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the save button click, creating or updating a facility.
     *
     * @param navController The NavController for navigation.
     */
    private void handleSaveButton(NavController navController) {
        String newName = binding.edittextName.getText().toString().trim();
        String newLocation = binding.edittextFacilityLocation.getText().toString().trim();

        if (!validateInputs(newName, newLocation)) return;

        boolean isNewFacility = userFacility == null;

        // If the user is creating a facility, create a  Facility object
        if (userFacility == null) {
            userFacility = new Facility(
                    currentUser.getDeviceId(),
                    UUID.randomUUID().toString(),
                    newName,
                    newLocation,
                    "",
                    ""
            );
            // If the user is editing an existing facility, update the fields
        } else {
            userFacility.setFacility_name(newName);
            userFacility.setFacility_location(newLocation);
        }

        // If the user has selected an image, upload it to Firebase
        if (selectedImageUri != null) {
            uploadImageToFirebase(selectedImageUri, new FacilityController.updateCallback() {
                @Override
                public void onSuccess() {
                    saveFacilityToDatabase(navController, isNewFacility);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("EditFacility", "Image upload error", e);
                }
            });
            // If no image is selected, just save the facility
        } else {
            saveFacilityToDatabase(navController, isNewFacility);
        }
    }

    /**
     * Saves the facility profile to the database.
     *
     * @param navController The NavController for navigation.
     */
    private void saveFacilityToDatabase(NavController navController, boolean isNewFacility) {
        // Ensure the facility is valid
        if (userFacility == null) return;

        // If the facility ID is empty, create a new facility
        if (isNewFacility) {
            facilityController.createFacility(userFacility, currentUser.getDeviceId(), new FacilityController.createCallback() {
                @Override
                public void onSuccess() {
                    currentUser.setOrganizer(true);
                    selectedImageUri = null; // Clear after save
                    navigateToViewFacility(navController, userFacility);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Failed to create facility.", Toast.LENGTH_SHORT).show();
                    Log.e("EditFacility", "Error creating facility", e);
                }
            });
            // If the facility ID is not empty, update an existing facility
        } else {
            facilityController.updateFacility(userFacility, new FacilityController.updateCallback() {
                @Override
                public void onSuccess() {
                    selectedImageUri = null; // Clear after save
                    navigateToViewFacility(navController, userFacility);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Failed to update facility.", Toast.LENGTH_SHORT).show();
                    Log.e("EditFacility", "Error updating facility", e);
                }
            });
        }
    }

    /**
     * Deletes the current facility image from Firebase Storage.
     */
    private void deleteImage() {
        if (userFacility != null && userFacility.getFacilityImagePath() != null && !userFacility.getFacilityImagePath().isEmpty()) {
            // Delete the image from Firebase Storage
            String imagePath = userFacility.getFacilityImagePath();
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
            imageRef.delete().addOnSuccessListener(aVoid -> {

                // Clear the facility image fields
                userFacility.setposterUri("");
                userFacility.setFacilityImagePath("");
                facilityController.updateField(userFacility, "posterUri", "");
                facilityController.updateField(userFacility, "facility_imagePath", "");

                // Reset the UI to show the placeholder image
                binding.imageviewEditFacilityImage.setImageResource(R.drawable.example_facility);
                binding.buttonDeleteFacilityImage.setVisibility(View.GONE);
                binding.imageviewEditFacilityImage.setClickable(true); // Re-enable image picker

                Toast.makeText(requireContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("EditFacilityFragment", "Error deleting image", e);
            });
        } else {
            Toast.makeText(requireContext(), "No image to delete.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates user inputs for creating or editing a facility.
     *
     * @param name     The facility name.
     * @param location The facility location.
     * @return True if inputs are valid, false otherwise.
     */
    private boolean validateInputs(String name, String location) {
        if (name.isEmpty()) {
            binding.edittextName.setError("Please enter a valid facility name.");
            return false;
        }
        if (location.isEmpty()) {
            binding.edittextFacilityLocation.setError("Please enter a valid facility location.");
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
                selectedImageUri = imageUri; // Store the selected image URI

                // Update the ImageView with the selected image
                Glide.with(requireContext())
                        .load(selectedImageUri)
                        .into(binding.imageviewEditFacilityImage);

                // Show the delete button since there's an image now
                binding.buttonDeleteFacilityImage.setVisibility(View.VISIBLE);

                Toast.makeText(requireContext(), "Image selected. Save to update.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Uploads the facility image to Firebase Storage.
     */
    private void uploadImageToFirebase(Uri imageUri, FacilityController.updateCallback callback) {
        String path = "images/" + UUID.randomUUID().toString();
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(path);

        // Upload the image to Firebase Storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (userFacility != null) {
                        userFacility.setposterUri(uri.toString());
                        userFacility.setFacilityImagePath(path);
                    }
                    callback.onSuccess();
                }))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Navigates to ViewFacilityFragment with the updated facility.
     *
     * @param navController The NavController for navigation.
     * @param facility      The updated facility object.
     */
    private void navigateToViewFacility(NavController navController, Facility facility) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("facility", facility);
        navController.navigate(R.id.action_EditFacilityFragment_to_ViewFacilityFragment, bundle);
    }
}
