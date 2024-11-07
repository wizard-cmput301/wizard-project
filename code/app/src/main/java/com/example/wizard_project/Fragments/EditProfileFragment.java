package com.example.wizard_project.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.PhotoHandler;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEditProfileBinding;
import com.example.wizard_project.Classes.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * EditProfileFragment allows the user to edit their profile information.
 */
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private User currentUser;
    private PhotoHandler photo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        photo = new PhotoHandler();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current user from MainActivity
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();

        binding.editProfileImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PhotoHandler.PICK_IMAGE_REQUEST);
            }
        });

        // Pre-fill the fields with the current user data
        if (currentUser != null) {
            binding.editTextName.setText(currentUser.getName());
            binding.editTextEmail.setText(currentUser.getEmail());
            binding.editTextPhone.setText(currentUser.getPhoneNumber());
            if (!currentUser.getProfilePictureUri().equals("")) {
                Uri imageUri = Uri.parse(currentUser.getProfilePictureUri());
                Glide.with(requireContext()).load(imageUri).circleCrop().into(binding.editProfileImage);
            }
        }

        // Set up the save button click listener
        binding.buttonSaveProfile.setOnClickListener(v -> saveUserProfile());

        binding.buttonDeleteProfilePic.setOnClickListener(v -> {
            String imagePath = currentUser.getProfilePath();
            if(!imagePath.equals("")){
                photo.deleteImage(imagePath, aVoid -> {
                            Toast.makeText(getContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show();
                            // Update user data and UI after successful deletion
                            currentUser.setProfilePictureUri("");
                            currentUser.setProfilePath("");
                            binding.editProfileImage.setImageResource(R.drawable.event_wizard_logo); // Set default image
                        },
                        e -> {
                            Toast.makeText(getContext(), "Failed to delete image", Toast.LENGTH_SHORT).show();
                        }
                );
            } else {
                Toast.makeText(getContext(), "No profile picture to delete", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Saves the updated user profile information and navigates back to ProfileFragment.
     */
    private void saveUserProfile() {
        String newName = binding.editTextName.getText().toString().trim();
        String newEmail = binding.editTextEmail.getText().toString().trim();
        String newPhone = binding.editTextPhone.getText().toString().trim();

        // TODO: Add validation for name, email, and phone number

        if (currentUser != null) {
            // Update the user's profile information in Firestore
            try {
                currentUser.setName(newName);
                currentUser.setEmail(newEmail);
                currentUser.setPhoneNumber(newPhone);

                // Show a confirmation toast
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                // Navigate back to ProfileFragment
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_EditProfileFragment_to_ProfileFragment);
            // Error handling (user data could not be updated)
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        // Error handling (user data not found)
        } else {
            Toast.makeText(requireContext(), "Failed to update profile. User data not available.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoHandler.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            currentUser.setProfilePictureUri(imageUri.toString());

            // Load the selected image into the ImageView
            Glide.with(requireContext()).load(imageUri).circleCrop().into(binding.editProfileImage);

            // Upload the image to Firebase
            PhotoHandler photo = new PhotoHandler();

            photo.uploadImage(currentUser, imageUri,
                    uri -> Toast.makeText(requireContext(), "Upload Success", Toast.LENGTH_SHORT).show(),
                    e -> Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT).show());

        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
