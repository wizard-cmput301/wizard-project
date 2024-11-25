package com.example.wizard_project.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentProfileBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ProfileFragment displays the user's profile information, and allows them to edit their information.
 * Additionally, admins can delete other users' profiles.
 */
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private User currentUser; // The current logged-in user
    private User displayUser; // The user whose data is displayed in this fragment

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using view binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current user from MainActivity
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        displayUser = currentUser; // Default to the current logged-in user

        NavController navController = Navigation.findNavController(view);

        // Determine if the user is viewing as an admin
        if (isViewingAsAdmin(navController)) {
            displayUser = mainActivity.getSelectedUser();
            setupAdminView(navController);
        } else {
            setupUserView(navController);
        }

        // Bind the display user's data to the UI
        bindUserDataToUI(displayUser);
    }

    /**
     * Checks if the current view is accessed by an admin.
     *
     * @param navController The NavController for navigation state checks.
     * @return True if the previous destination was AdminFragment.
     */
    private boolean isViewingAsAdmin(NavController navController) {
        int previousDestinationId = navController.getPreviousBackStackEntry().getDestination().getId();
        return previousDestinationId == R.id.AdminFragment;
    }

    /**
     * Sets up the admin view, showing options to delete the displayed user.
     *
     * @param navController The NavController for navigation.
     */
    private void setupAdminView(NavController navController) {
        binding.buttonDeleteProfile.setVisibility(View.VISIBLE); // Show delete button
        binding.buttonEditProfile.setVisibility(View.INVISIBLE); // Hide edit button

        binding.buttonDeleteProfile.setOnClickListener(v -> deleteProfile(displayUser, navController));
    }

    /**
     * Sets up the regular user view, allowing profile editing.
     *
     * @param navController The NavController for navigation.
     */
    private void setupUserView(NavController navController) {
        binding.buttonDeleteProfile.setVisibility(View.GONE); // Hide delete button

        binding.buttonEditProfile.setOnClickListener(v ->
                navController.navigate(R.id.action_ProfileFragment_to_EditProfileFragment)
        );
    }

    /**
     * Binds the given user's data to the UI elements.
     *
     * @param user The user whose data is being displayed.
     */
    private void bindUserDataToUI(User user) {
        if (user != null) {
            binding.textviewProfileName.setText(
                    user.getName() != null && !user.getName().isEmpty() ? user.getName() : "No name provided"
            );
            binding.textviewProfileEmail.setText(
                    user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : "No email provided"
            );
            binding.textviewProfilePhone.setText(
                    user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty() ? user.getPhoneNumber() : "No phone number provided"
            );

            if (user.getProfilePictureUri() != null && !user.getProfilePictureUri().isEmpty()) {
                Glide.with(requireContext())
                        .load(Uri.parse(user.getProfilePictureUri()))
                        .circleCrop()
                        .into(binding.imageviewProfileImage);
            } else {
                binding.imageviewProfileImage.setImageResource(R.drawable.event_wizard_logo); // Placeholder image
            }
        } else {
            binding.textviewProfileName.setText("User not found");
            Toast.makeText(requireContext(), "User data is not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes the profile of the specified user.
     *
     * @param userToDelete The user to delete.
     * @param navController The NavController for navigation after deletion.
     */
    private void deleteProfile(User userToDelete, NavController navController) {
        if (userToDelete == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userToDelete.getDeviceId());

        userToDelete.deleteUser(); // Clear in-memory data

        docRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("ProfileFragment", "Profile deleted successfully");
                    Toast.makeText(requireContext(), "Profile deleted.", Toast.LENGTH_SHORT).show();
                    navController.navigate(R.id.action_ProfileFragment_to_AdminFragment);
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Error deleting profile", e);
                    Toast.makeText(requireContext(), "Error deleting profile.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}
