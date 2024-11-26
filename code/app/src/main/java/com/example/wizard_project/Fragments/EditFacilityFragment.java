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
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.Classes.PhotoHandler;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.FacilityController;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEditFacilityBinding;
import com.example.wizard_project.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditFacilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        String userId = currentUser.getDeviceId();
        EditText facilityName = binding.facilityEditName;
        EditText facilityLocation = binding.facilityEditLocation;
        Button doneButton = binding.facilityDoneButton;
        Button uploadImageButton = binding.facilityEditImageButton;
        ImageView facilityImage = binding.facilityEditImageview;
        // Populate the text fields with existing facility info.
        controller.getFacility(userId, new FacilityController.facilityCallback() {
            @Override
            public void onCallback(Facility facility) {
                if (facility != null) {
                    userFacility = facility;
                    facilityName.setText(userFacility.getFacility_name());
                    facilityLocation.setText(userFacility.getFacility_location());
                    if(userFacility.getFacilitymagePath() != null){
                        Uri imageUri = Uri.parse(facility.getposterUri());
                        Glide.with(requireContext()).load(imageUri).into(facilityImage);
                    }
                }else{
                    userFacility = new Facility(currentUser.getDeviceId(),UUID.randomUUID().toString(),"","","","");
                }
            }
        });

        // Submit the inputted facility info, direct user to facility view.
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = facilityName.getText().toString();
                String newLocation = facilityLocation.getText().toString();

                if (newName.trim().isEmpty()) {
                    facilityName.setError("Please enter a valid facility name.");

                } else if (newLocation.trim().isEmpty()) {
                    facilityLocation.setError("Please enter a valid location.");

                } else {
                    if (currentUser.isOrganizer()) {
                        // Update existing facility info if the user is already an organizer.
                        userFacility.setFacility_name(newName);
                        userFacility.setFacility_location(newLocation);
                        controller.updateFacility(userFacility);
                        navController.navigate(R.id.action_EditFacilityFragment_to_ViewFacilityFragment);

                    } else {
                        // Create new facility with the inputted info.
                        controller.createFacility(userFacility);
                        currentUser.setOrganizer(true);
                        navController.navigate(R.id.action_EditFacilityFragment_to_ViewFacilityFragment);
                    }
                }
            }
        });

        // Prompt the user to select a photo and add it to the database.
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PhotoHandler.PICK_IMAGE_REQUEST);
            }
        });
    }
    /**
     * Uploads the selected image to Firebase and updates the facility in the UI.
     *
     * @param requestCode The request code identifying the image pick request.
     * @param resultCode The result code indicating success or failure.
     * @param data The Intent data returned by the image picker.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoHandler.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {


            if (!userFacility.getFacilitymagePath().isEmpty() && !userFacility.getposterUri().isEmpty()){
                String imagePath = userFacility.getFacilitymagePath();
                // Get a reference to the image in Firebase Storage
                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                // Delete the image
                imageRef.delete();
            }

            Uri imageUri = data.getData();
            userFacility.setposterUri(imageUri.toString());

            // Load the selected image into the ImageView
            Glide.with(requireContext()).load(imageUri).into(binding.facilityEditImageview);
            // update the database with new image
            String path = "images/" + UUID.randomUUID().toString();
            userFacility.setFacilityImagePath(path);
            userFacility.setposterUri(imageUri.toString());
            controller.updateFeild(userFacility,"posterUri",imageUri.toString());
            controller.updateFeild(userFacility,"facility_imagePath",path);

            // Upload image to Firebase Storage
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(path);
            imageRef.putFile(imageUri).addOnFailureListener(e -> {
                // Handle failure
                Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        }
    }
}
