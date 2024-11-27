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
    private PhotoHandler photoHandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditFacilityBinding.inflate(inflater, container, false);
        photoHandler = new PhotoHandler(); // Initialize photoHandler
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current user
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        NavController navController = NavHostFragment.findNavController(this);

        // Initialize UI
        initializeUI(navController);
    }

    /**
     * Initializes the UI elements and populates them based on the existing facility (if any).
     */
    private void initializeUI(NavController navController) {
        // Get facility passed to the fragment
        userFacility = (Facility) (getArguments() != null ? getArguments().getSerializable("facility") : null);

        if (userFacility == null) {
            // Set up for creating a new facility
            binding.edittextName.setText("");
            binding.edittextFacilityLocation.setText("");
            binding.buttonSaveFacility.setText("Create Facility");
        } else {
            // Populate fields for editing existing facility
            binding.edittextName.setText(userFacility.getFacility_name());
            binding.edittextFacilityLocation.setText(userFacility.getFacility_location());
            binding.buttonSaveFacility.setText("Save Changes");

            // Load existing facility image if available
            if (userFacility.getFacilityImagePath() != null && !userFacility.getFacilityImagePath().isEmpty()) {
                Glide.with(requireContext()).load(userFacility.getFacilityImagePath()).into(binding.imageviewEditFacilityImage);
            }
        }

        // Set up listeners
        setupListeners(navController);
    }

    /**
     * Sets up the event listeners for UI elements.
     */
    private void setupListeners(NavController navController) {
        // Image selection listener
        binding.imageviewEditFacilityImage.setOnClickListener(v -> openImagePicker());

        // Save button listener
        binding.buttonSaveFacility.setOnClickListener(v -> handleSaveButton(navController));
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
     */
    private void handleSaveButton(NavController navController) {
        String newName = binding.edittextName.getText().toString().trim();
        String newLocation = binding.edittextFacilityLocation.getText().toString().trim();

        if (!validateInputs(newName, newLocation)) return;

        if (userFacility == null) {
            // Create a new facility
            userFacility = new Facility(
                    currentUser.getDeviceId(),
                    UUID.randomUUID().toString(),
                    newName,
                    newLocation,
                    "",
                    ""
            );

            facilityController.createFacility(userFacility, currentUser.getDeviceId(), new FacilityController.createCallback() {
                @Override
                public void onSuccess() {
                    currentUser.setOrganizer(true);
                    navigateToViewFacility(navController, userFacility);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Failed to create facility.", Toast.LENGTH_SHORT).show();
                    Log.e("EditFacility", "Error creating facility", e);
                }
            });
        } else {
            // Update existing facility
            userFacility.setFacility_name(newName);
            userFacility.setFacility_location(newLocation);

            facilityController.updateFacility(userFacility, new FacilityController.updateCallback() {
                @Override
                public void onSuccess() {
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


            if (!userFacility.getFacilityImagePath().isEmpty() && !userFacility.getposterUri().isEmpty()){
                String imagePath = userFacility.getFacilityImagePath();
                // Get a reference to the image in Firebase Storage
                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                // Delete the image
                imageRef.delete();
            }

            Uri imageUri = data.getData();
            if (imageUri != null) {
                String path = "images/" + UUID.randomUUID().toString();
                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(path);

                // Upload image to Firebase Storage
                imageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            userFacility.setposterUri(uri.toString());
                            userFacility.setFacilityImagePath(path);

                            facilityController.updateField(userFacility, "posterUri", uri.toString());
                            facilityController.updateField(userFacility, "facility_imagePath", path);

                            // Update ImageView
                            Glide.with(requireContext()).load(uri).into(binding.imageviewEditFacilityImage);
                            Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("EditFacility", "Image upload error", e);
                        });
            }
        }
    }

    /**
     * Uploads an image to Firebase Storage and updates the facility's image path.
     *
     * @param imageUri The URI of the selected image.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        String path = "images/" + UUID.randomUUID().toString();
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(path);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (userFacility != null) {
                        userFacility.setposterUri(uri.toString());
                        userFacility.setFacilityImagePath(path);

                        facilityController.updateField(userFacility, "posterUri", uri.toString());
                        facilityController.updateField(userFacility, "facility_imagePath", path);
                    }
                    Glide.with(requireContext()).load(uri).into(binding.imageviewEditFacilityImage);
                    Toast.makeText(requireContext(), "Image uploaded successfully.", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("EditFacility", "Image upload error", e);
                });
    }

    /**
     * Navigates to the ViewFacilityFragment with the updated facility.
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
