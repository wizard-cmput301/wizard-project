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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEditFacilityBinding;
import com.example.wizard_project.MainActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

/**
 * EditFacilityFragment allows an organizer to create or edit a facility.
 */
public class EditFacilityFragment extends Fragment {
    private FragmentEditFacilityBinding binding;
    private User currentUser;
    private Facility userFacility;
    private final FacilityController controller = new FacilityController();
    private PhotoHandler photo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditFacilityBinding.inflate(inflater, container, false);
        photo = new PhotoHandler(); // Initialize the photo handler
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current user
        NavController navController = NavHostFragment.findNavController(this);
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        String userId = currentUser.getDeviceId();

        // Initialize UI elements
        ImageView facilityImage = binding.imageviewEditFacilityImage;
        EditText facilityName = binding.edittextFacilityName;
        EditText facilityLocation = binding.edittextFacilityLocation;
        Button saveButton = binding.buttonSaveFacility;

        // Get the facility passed to the fragment (if any)
        userFacility = (Facility) (getArguments() != null ? getArguments().getSerializable("facility") : null);

        // Check if the user is editing an existing facility, else create a new one.
        if (userFacility == null) {
            // Create a new facility
            facilityName.setText("");
            facilityLocation.setText("");
            saveButton.setText("Create Facility");
        } else {
            // Edit an existing facility
            if (userFacility != null) {
                facilityName.setText(userFacility.getFacility_name());
                facilityLocation.setText(userFacility.getFacility_location());
                saveButton.setText("Save Changes");
                if (userFacility.getFacilitymagePath() != null && !userFacility.getFacilitymagePath().isEmpty()) {
                    Glide.with(requireContext()).load(userFacility.getFacilitymagePath()).into(facilityImage);
                }
            }
        }

        // Handle image selection
        facilityImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PhotoHandler.PICK_IMAGE_REQUEST);
        });

        // Set up save button listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = facilityName.getText().toString();
                String newLocation = facilityLocation.getText().toString();

                // Input Validation TODO: Add more validation
                if (newName.isEmpty()) {
                    facilityName.setError("Please enter a valid facility name.");
                    return;
                }
                if (newLocation.isEmpty()) {
                    facilityLocation.setError("Please enter a valid facility location.");
                    return;
                }

                // If the user is not an organizer, create a new facility.
                if (!currentUser.isOrganizer()) {
                    // Create a new facility object with the inputted info.
                    userFacility = new Facility(
                            currentUser.getDeviceId(),
                            UUID.randomUUID().toString(),
                            newName,
                            newLocation,
                            "",
                            ""
                    );
                    controller.createFacility(userFacility, currentUser.getDeviceId(), new FacilityController.createCallback() {
                        @Override
                        public void onSuccess() {
                            currentUser.setOrganizer(true);
                            navigateToViewFacility(navController, userFacility);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(), "Failed to create facility", Toast.LENGTH_SHORT).show();
                            Log.e("EditFacility", "Error creating facility", e);
                        }
                    });
                } else {
                    // If the user is an organizer, update the existing facility.
                    userFacility.setFacility_name(newName);
                    userFacility.setFacility_location(newLocation);

                    controller.updateFacility(userFacility, new FacilityController.updateCallback() {
                        @Override
                        public void onSuccess() {
                            navigateToViewFacility(navController, userFacility);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(), "Failed to update facility", Toast.LENGTH_SHORT).show();
                            Log.e("EditFacility", "Error updating facility", e);
                        }
                    });
                }
            }
        });
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
                            userFacility.setposterUri(uri.toString());
                            userFacility.setFacilityImagePath(path);

                            controller.updateField(userFacility, "posterUri", uri.toString());
                            controller.updateField(userFacility, "facility_imagePath", path);

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
     * Navigates to the ViewFacilityFragment with the updated facility.
     */
    private void navigateToViewFacility(NavController navController, Facility facility) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("facility", facility);
        navController.navigate(R.id.action_EditFacilityFragment_to_ViewFacilityFragment, bundle);
    }
}
