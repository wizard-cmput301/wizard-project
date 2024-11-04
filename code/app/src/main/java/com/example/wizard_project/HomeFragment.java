package com.example.wizard_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.databinding.FragmentHomeBinding;

/**
 * HomeFragment represents the screen that users see when they open the app.
 * This fragment displays the app name and logo, and has buttons to navigate to
 * the entrant, organizer, and admin screens.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the NavController for navigating between fragments
        NavController navController = NavHostFragment.findNavController(this);

        // Button to navigate to EntrantFragment
        binding.enterEventButton.setOnClickListener(v -> navController.navigate(R.id.action_HomeFragment_to_EntrantFragment));

        // Button to navigate to OrganizerFragment
        binding.manageFacilityButton.setOnClickListener(v -> navController.navigate(R.id.action_HomeFragment_to_OrganizerFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
