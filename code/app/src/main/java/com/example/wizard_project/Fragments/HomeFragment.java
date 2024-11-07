package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * HomeFragment represents the screen that users see when they open the app.
 * This fragment displays the app name and logo, and has buttons to navigate to
 * the entrant, organizer, and admin screens.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding; // View binding accessing UI elements
    private FirebaseFirestore db; // Firestore instance for database operations

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

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

        // Check if the user is an admin
        isOrganizer(isOrganizer -> {
            // If the user is an organizer, show the button to navigate to OrganizerFragment
            if (isOrganizer) {
                binding.manageFacilityButton.setOnClickListener(v -> navController.navigate(R.id.action_HomeFragment_to_ViewFacilityFragment));
            }
            // If the user is not an admin, hide the admin button
            else {
                binding.manageFacilityButton.setOnClickListener(v -> navController.navigate(R.id.action_HomeFragment_to_EditFacilityFragment));
            }
        });


        // Check if the user is an admin
        isAdmin(isAdmin -> {
            // If the user is an admin, show the button to navigate to AdminFragment
            if (isAdmin) {
                binding.adminButton.setVisibility(View.VISIBLE);
                binding.adminButton.setOnClickListener(v -> navController.navigate(R.id.action_HomeFragment_to_AdminFragment));
            }
            // If the user is not an admin, hide the admin button
            else {
                binding.adminButton.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Checks if the user is an admin in the database.
     *
     * @param callback The callback to handle the result.
     */
    private void isAdmin(AdminCheckCallback callback) {
        // Get the device ID from the MainActivity
        String deviceId = ((MainActivity) requireActivity()).retrieveDeviceId();

        // Query the database for the user document
        db.collection("users").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            // Check if the document exists in the database
            if (documentSnapshot.exists()) {
                // Get the 'admin' field from the document, pass the result to the callback
                Boolean isAdmin = documentSnapshot.getBoolean("IsAdmin");
                callback.onResult(isAdmin != null && isAdmin);
            } else {
                callback.onResult(false); // If document does not exist, assume not admin
            }
        }).addOnFailureListener(e -> {
            callback.onResult(false); // If error, assume not admin
        });
    }

    /**
     * Checks if the user is an organizer in the database.
     *
     * @param callback The callback to handle the result.
     */
    private void isOrganizer(organizerCheckCallback callback) {
        // Get the device ID from the MainActivity
        String deviceId = ((MainActivity) requireActivity()).retrieveDeviceId();

        // Query the database for the user document
        db.collection("users").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            // Check if the document exists in the database
            if (documentSnapshot.exists()) {
                // Get the 'organizer' field from the document, pass the result to the callback
                Boolean isOrganizer = documentSnapshot.getBoolean("IsOrganizer");
                callback.onResult(isOrganizer != null && isOrganizer);
            } else {
                callback.onResult(false); // If document does not exist, assume not organizer
            }
        }).addOnFailureListener(e -> {
            callback.onResult(false); // If error, assume not organizer
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Callback interface to handle the result of the admin check.
     */
    public interface AdminCheckCallback {
        void onResult(boolean isAdmin);
    }

    /**
     * Callback interface to handle the result of the organizer check.
     */
    public interface organizerCheckCallback {
        void onResult(boolean isOrganizer);
    }
}