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
    private PhotoHandler photoHandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        photoHandler = new PhotoHandler(); // Initialize photoHandler
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current user from MainActivity
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();

        // Pre-fill profile fields with current user data
        populateUserProfile();

        // Set up listeners
        setupListeners();
    }

    /**
     * Pre-fills the profile fields with the current user's data, if available.
     */
    private void populateUserProfile() {
        if (currentUser != null) {
            binding.edittextName.setText(currentUser.getName());
            binding.edittextEmail.setText(currentUser.getEmail());
            binding.edittextPhone.setText(currentUser.getPhoneNumber());

            String profilePictureUri = currentUser.getProfilePictureUri();
            if (profilePictureUri != null && !profilePictureUri.isEmpty()) {
                // Load existing profile picture
                Glide.with(requireContext())
                        .load(Uri.parse(profilePictureUri))
                        .circleCrop()
                        .into(binding.imageviewProfilePicture);
                binding.framelayoutProfilePictureContainer.setClickable(false); // Disable image picker
            } else if (!currentUser.getName().isEmpty()){
                int draw = currentUser.profilePictureGenerator();
                Glide.with(this).load(draw).circleCrop().into(binding.imageviewProfilePicture);
                binding.framelayoutProfilePictureContainer.setClickable(true); // Enable image picker
            } else {
                Glide.with(this).load(R.drawable.noname).circleCrop().into(binding.imageviewProfilePicture);
                binding.framelayoutProfilePictureContainer.setClickable(true); // Enable image picker
            }
        }
    }

    /**
     * Sets up listeners for profile picture selection, saving changes, and deleting the profile picture.
     */
    private void setupListeners() {
        // Profile picture selection
        binding.framelayoutProfilePictureContainer.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getProfilePictureUri() != null && !currentUser.getProfilePictureUri().isEmpty()) {
                Toast.makeText(requireContext(), "Please delete your current profile picture before uploading a new one.", Toast.LENGTH_SHORT).show();
            } else {
                openImagePicker();
            }
        });

        // Save profile changes
        binding.buttonSaveProfile.setOnClickListener(v -> saveUserProfile());

        // Delete profile picture
        binding.buttonDeleteProfilePic.setOnClickListener(v -> deleteProfilePicture());
    }

    /**
     * Opens an image picker to select a profile picture.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PhotoHandler.PICK_IMAGE_REQUEST);
    }


    /**
     * Saves the updated profile information to the user object and navigates back to the ProfileFragment.
     */
    private void saveUserProfile() {
        String newName = binding.edittextName.getText().toString().trim();
        String newEmail = binding.edittextEmail.getText().toString().trim();
        String newPhone = binding.edittextPhone.getText().toString().trim();

        if (!validateInputs(newName, newEmail)) return;

        if (currentUser != null) {
            // Update user information
            currentUser.setName(newName);
            currentUser.setEmail(newEmail);
            currentUser.setPhoneNumber(newPhone);

            Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();

            // Navigate back to ProfileFragment
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_EditProfileFragment_to_ProfileFragment);
        } else {
            Toast.makeText(requireContext(), "Failed to update profile. User data not available.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates user inputs for the profile fields.
     *
     * @param name  The user's name.
     * @param email The user's email address.
     * @return True if all inputs are valid; false otherwise.
     */
    private boolean validateInputs(String name, String email) {
        if (name.isEmpty()) {
            binding.edittextName.setError("Name cannot be empty.");
            return false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edittextEmail.setError("Enter a valid email address.");
            return false;
        }
        return true;
    }

    /**
     * Deletes the user's profile picture and updates the UI.
     */
    private void deleteProfilePicture() {
        String imagePath = currentUser.getProfilePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            photoHandler.deleteImage(imagePath,
                    aVoid -> {
                        currentUser.setProfilePictureUri("");
                        currentUser.setProfilePath("");

                        binding.imageviewProfilePicture.setImageResource(R.drawable.noname); // Default placeholder
                        binding.framelayoutProfilePictureContainer.setClickable(true); // Enable image picker
                        Toast.makeText(requireContext(), "Profile picture deleted successfully.", Toast.LENGTH_SHORT).show();
                    },
                    e -> Toast.makeText(requireContext(), "Failed to delete profile picture.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(requireContext(), "No profile picture to delete.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Uploads the selected image to Firebase and updates the profile picture in the UI.
     *
     * @param requestCode The request code identifying the image pick request.
     * @param resultCode The result code indicating success or failure.
     * @param data The Intent data returned by the image picker.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is a successful image picker result
        if (requestCode == PhotoHandler.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {

            // Ensure the fragment is attached to avoid context issues (implemented for testing)
            if (!isAdded() || isDetached()) {
                Log.w("EditProfileFragment", "Fragment not attached. Skipping image handling.");
                return; // Skip image handling if the fragment is not attached
            }

            Uri imageUri = data.getData(); // Get the selected image URI
            if (imageUri != null) {

                // Delete the old profile picture if it exists
                if (!currentUser.getProfilePictureUri().isEmpty() && !currentUser.getProfilePath().isEmpty()) {
                    String imagePath = currentUser.getProfilePath();
                    StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                    imageRef.delete();
                }

                // Update the user's profile picture URI
                currentUser.setProfilePictureUri(imageUri.toString());
                Glide.with(requireContext()).load(imageUri).circleCrop().into(binding.imageviewProfilePicture);

                // Upload the new profile picture to Firebase
                PhotoHandler photo = new PhotoHandler();
                photo.uploadImage(currentUser, imageUri,
                        uri -> {
                            if (isAdded() && !isDetached()) { // Double-check before interacting with the context
                                Toast.makeText(requireContext(), "Upload Success", Toast.LENGTH_SHORT).show();
                            }
                        },
                        e -> {
                            if (isAdded() && !isDetached()) { // Double-check before interacting with the context
                                Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    /**
     * Uploads the selected profile picture to Firebase and updates the user's profile picture URI.
     *
     * @param imageUri The URI of the selected image.
     */
    private void uploadProfilePicture(Uri imageUri) {
        photoHandler.uploadImage(currentUser, imageUri,
                uri -> {
                    currentUser.setProfilePictureUri(uri.toString());
                    Glide.with(requireContext()).load(uri).circleCrop().into(binding.imageviewProfilePicture);
                    Toast.makeText(requireContext(), "Profile picture updated successfully.", Toast.LENGTH_SHORT).show();
                },
                e -> Toast.makeText(requireContext(), "Failed to upload profile picture.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
