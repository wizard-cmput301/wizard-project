package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEditProfileBinding;
import com.example.wizard_project.Classes.User;

/**
 * EditProfileFragment allows the user to edit their profile information.
 */
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current user from MainActivity
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();

        // Pre-fill the fields with the current user data
        if (currentUser != null) {
            binding.editTextName.setText(currentUser.getName());
            binding.editTextEmail.setText(currentUser.getEmail());
            binding.editTextPhone.setText(currentUser.getPhoneNumber());
        }

        // Set up the save button click listener
        binding.buttonSaveProfile.setOnClickListener(v -> saveUserProfile());
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
