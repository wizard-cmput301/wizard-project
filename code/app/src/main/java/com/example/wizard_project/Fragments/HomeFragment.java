package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Controllers.FacilityController;
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
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the NavController for navigating between fragments
        NavController navController = NavHostFragment.findNavController(this);


        setupNavigationButtons(navController);
        setupUserRoles(navController);
    }

    /**
     * Sets up navigation for the entrant and QR code scanner buttons.
     *
     * @param navController The NavController for navigating between fragments.
     */
    private void setupNavigationButtons(NavController navController) {
        binding.enterEventButton.setOnClickListener(v ->
                navController.navigate(R.id.action_HomeFragment_to_EntrantEventFragment)
        );

        binding.qrcodeButton.setOnClickListener(v ->
                navController.navigate(R.id.action_HomeFragment_to_QRScannerFragment)
        );
    }

    /**
     * Configures visibility and navigation based on the user's roles.
     *
     * @param navController The NavController for navigating between fragments.
     */
    private void setupUserRoles(NavController navController) {
        checkIfOrganizer(isOrganizer -> {
            if (binding != null) { // Ensure binding is valid (for test cases)
                if (isOrganizer) {
                    configureOrganizerButton(navController);
                } else {
                    binding.manageFacilityButton.setOnClickListener(v ->
                            navController.navigate(R.id.action_HomeFragment_to_EditFacilityFragment)
                    );
                }
            }
        });

        checkIfAdmin(isAdmin -> {
            if (binding != null) { // Ensure binding is still valid (for test cases)
                binding.adminButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                if (isAdmin) {
                    binding.adminButton.setOnClickListener(v ->
                            navController.navigate(R.id.action_HomeFragment_to_AdminFragment)
                    );
                }
            }
        });
    }

    /**
     * Configures the manage facility button for organizers.
     *
     * @param navController The NavController for navigating between fragments.
     */
    private void configureOrganizerButton(NavController navController) {
        fetchFacilityForOrganizer(facility -> {
            if (facility != null) {
                binding.manageFacilityButton.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("facility", facility);
                    navController.navigate(R.id.action_HomeFragment_to_ViewFacilityFragment, bundle);
                });
            } else {
                Toast.makeText(requireContext(), "Facility not found. Please create one.", Toast.LENGTH_SHORT).show();
                binding.manageFacilityButton.setOnClickListener(v ->
                        navController.navigate(R.id.action_HomeFragment_to_EditFacilityFragment)
                );
            }
        });
    }

    /**
     * Fetches the facility associated with the organizer.
     *
     * @param callback A callback to handle the retrieved facility.
     */
    private void fetchFacilityForOrganizer(FacilityController.facilityCallback callback) {
        String userId = ((MainActivity) requireActivity()).retrieveDeviceId();
        FacilityController facilityController = new FacilityController();

        facilityController.getFacility(userId, callback::onCallback);
    }

    /**
     * Checks if the current user has admin privileges.
     *
     * @param callback A callback to handle the result of the admin check.
     */
    private void checkIfAdmin(AdminCheckCallback callback) {
        String deviceId = ((MainActivity) requireActivity()).retrieveDeviceId();

        db.collection("users").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            Boolean isAdmin = documentSnapshot.getBoolean("IsAdmin");
            callback.onResult(Boolean.TRUE.equals(isAdmin));
        }).addOnFailureListener(e -> callback.onResult(false));
    }

    /**
     * Checks if the user is an organizer.
     *
     * @param callback A callback to handle the result of the organizer check.
     */
    private void checkIfOrganizer(OrganizerCheckCallback callback) {
        String deviceId = ((MainActivity) requireActivity()).retrieveDeviceId();

        db.collection("users").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            Boolean isOrganizer = documentSnapshot.getBoolean("isOrganizer");
            callback.onResult(Boolean.TRUE.equals(isOrganizer));
        }).addOnFailureListener(e -> callback.onResult(false));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear binding to avoid memory leaks
    }

    /**
     * Callback interface for checking admin privileges.
     */
    public interface AdminCheckCallback {
        void onResult(boolean isAdmin);
    }

    /**
     * Callback interface for checking organizer privileges.
     */
    public interface OrganizerCheckCallback {
        void onResult(boolean isOrganizer);
    }
}
