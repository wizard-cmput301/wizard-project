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
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

        // Set up buttons to navigate to other fragments
        binding.enterEventButton.setOnClickListener(v -> navController.navigate(R.id.action_HomeFragment_to_EntrantFragment)); // Navigate to EntrantFragment
        binding.qrcodeButton.setOnClickListener(v -> navController.navigate(R.id.action_HomeFragment_to_QRScannerFragment)); // Navigate to QRScannerFragment
        binding.manageFacilityButton.setOnClickListener(v -> navController.navigate(R.id.action_HomeFragment_to_OrganizerFragment)); // Navigate to OrganizerFragment

        // Display the admin button if the user has admin privileges
        isAdmin(isAdmin -> {
            if (binding != null) { // Ensure binding is still valid
                if (isAdmin) {
                    binding.adminButton.setVisibility(View.VISIBLE);
                    binding.adminButton.setOnClickListener(v -> navController.navigate(R.id.action_HomeFragment_to_AdminFragment));
                } else {
                    binding.adminButton.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Checks if the current user has admin privileges by querying Firestore.
     * If the user is an admin, the admin button is made visible.
     *
     * @param callback A callback to handle the result of the admin check.
     */
    private void isAdmin(AdminCheckCallback callback) {
        // Get the device ID from the MainActivity
        String deviceId = ((MainActivity) requireActivity()).retrieveDeviceId();

        // Query Firestore to check if the user has admin privileges
        db.collection("users").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean isAdmin = documentSnapshot.getBoolean("IsAdmin");
                callback.onResult(isAdmin != null && isAdmin);
            } else {
                callback.onResult(false); // If document does not exist, assume not admin
            }
        }).addOnFailureListener(e -> {
            callback.onResult(false); // If error, assume not admin
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
}
