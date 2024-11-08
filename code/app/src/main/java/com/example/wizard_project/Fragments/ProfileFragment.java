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
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.PhotoHandler;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ProfileFragment displays the user's profile information, and allows them to edit their information.
 * Additionally, admins can delete other users' profiles.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private User currentUser; // The current logged-in user
    private User deleteUser; // The user being viewed or deleted, if the current user is an admin
    private User DisplayUser; // The user whose data is displayed in this fragment

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using view binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current user and deleteUser information from MainActivity
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        deleteUser = mainActivity.getDeleteUser();
        DisplayUser = currentUser; // Default to currentUser unless admin is viewing another profile

        // Check if the user is viewing as an admin or as an entrant
        NavController navController = Navigation.findNavController(view);
        int previousDestinationId =  navController.getPreviousBackStackEntry().getDestination().getId();

        // If the user is viewing as an admin, replace the edit button with a delete button
        if (previousDestinationId == R.id.AdminFragment) {
            DisplayUser = deleteUser;
            binding.deleteProfileButton.setVisibility(View.VISIBLE);
            binding.editProfileButton.setVisibility(View.INVISIBLE);
            binding.deleteProfileButton.setOnClickListener(v -> DeleteProfile());
        }

        // Display profile data if available
        if (DisplayUser != null) {
            binding.profileName.setText(DisplayUser.getName() != null && !DisplayUser.getName().isEmpty() ? DisplayUser.getName() : "No name provided"); // Display user's name
            binding.profileEmail.setText(DisplayUser.getEmail() != null && !DisplayUser.getEmail().isEmpty() ? DisplayUser.getEmail() : "No email provided"); // Display user's email
            binding.profilePhone.setText(DisplayUser.getPhoneNumber() != null && !DisplayUser.getPhoneNumber().isEmpty() ? DisplayUser.getPhoneNumber() : "No phone number provided"); // Display user's phone number

            // Load and display the user's profile picture if available
            if (!DisplayUser.getProfilePictureUri().equals("")) {
                Uri imageUri = Uri.parse(DisplayUser.getProfilePictureUri());
                Glide.with(requireContext()).load(imageUri).circleCrop().into(binding.profileImage);
            }
        // Error handling if the user data is not available
        } else {
            binding.profileName.setText("User not found");
            Toast.makeText(requireContext(), "User data is not available", Toast.LENGTH_SHORT).show();
        }

        // Navigate to EditProfileFragment when edit profile button is clicked
        binding.editProfileButton.setOnClickListener(v -> { navController.navigate(R.id.action_ProfileFragment_to_EditProfileFragment);
        });
    }

    /**
     * Deletes the profile of the user currently being viewed by an admin.
     * After deletion, navigates back to the admin view.
     */
    private void DeleteProfile() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(deleteUser.getDeviceId());

        // Clear the user's data in memory
        deleteUser.DeleteUser();

        // Delete the user's document from Firestore
        docRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Document successfully deleted!");
                        // Navigate to HomeFragment after successful deletion
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_ProfileFragment_to_AdminFragment);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error deleting document", e);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
