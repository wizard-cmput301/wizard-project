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

import com.example.wizard_project.Classes.User;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentProfileBinding;

/**
 * ProfileFragment displays the user's profile information, and allows them to edit their information.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current user information from MainActivity
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();

        // Display user information in the UI if available
        if (currentUser != null) {
            // Name
            binding.profileName.setText(currentUser.getName() != null && !currentUser.getName().isEmpty() ? currentUser.getName() : "No name provided");

            // Email
            binding.profileEmail.setText(currentUser.getEmail() != null && !currentUser.getEmail().isEmpty() ? currentUser.getEmail() : "No email provided");

            // Phone number
            binding.profilePhone.setText(currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().isEmpty() ? currentUser.getPhoneNumber() : "No phone number provided");

            // TODO: Load and display the user's profile picture here
        // Error handling (user data not found)
        } else {
            binding.profileName.setText("User not found");
            Toast.makeText(requireContext(), "User data is not available", Toast.LENGTH_SHORT).show();
        }

        // Navigate to EditProfileFragment when the edit profile button is clicked
        binding.editProfileButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_ProfileFragment_to_EditProfileFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}