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

        // Check if the user is viewing as an admin
        NavController navController = Navigation.findNavController(view);
        int previousDestinationId = navController.getPreviousBackStackEntry().getDestination().getId();

        // If viewing as admin, display the chosen user's data
        if (previousDestinationId == R.id.AdminFragment) {
            displayUser = mainActivity.getSelectedUser();
            binding.buttonDeleteProfile.setVisibility(View.VISIBLE); // Show delete button for admins
            binding.buttonEditProfile.setVisibility(View.INVISIBLE); // Hide edit button for admins
            binding.buttonDeleteProfile.setOnClickListener(v -> DeleteProfile(displayUser));
        }

        // Bind the displayUser data to the UI
        bindUserDataToUI(displayUser);

        // Navigate to EditProfileFragment when edit profile button is clicked
        binding.buttonEditProfile.setOnClickListener(v -> {
            navController.navigate(R.id.action_ProfileFragment_to_EditProfileFragment);
        });
    }

    /**
     * Binds the given user's data to the UI elements in the fragment.
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

            // Load and display the user's profile picture if available
            if (user.getProfilePictureUri() != null && !user.getProfilePictureUri().isEmpty()) {
                Uri imageUri = Uri.parse(user.getProfilePictureUri());
                Glide.with(requireContext()).load(imageUri).circleCrop().into(binding.imageviewProfileImage);
            }
        } else {
            binding.textviewProfileName.setText("User not found");
            Toast.makeText(requireContext(), "User data is not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes the profile of the user currently being viewed by an admin.
     * After deletion, navigates back to the admin view.
     */
    private void DeleteProfile(User userToDelete) {
        if (userToDelete == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userToDelete.getDeviceId());

        // Clear the user's data in memory
        userToDelete.DeleteUser();

        // Delete the user's document from Firestore
        docRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("TAG", "Document successfully deleted!");
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_ProfileFragment_to_AdminFragment);
                })
                .addOnFailureListener(e -> {
                    Log.w("TAG", "Error deleting document", e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
