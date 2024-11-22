package com.example.wizard_project.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.FacilityController;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentViewFacilityBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ViewFacilityFragment displays a facility's information, and adjusts the UI based on the user's role:
 *  Entrant: Can view a facility linked to an event.
 *  Organizer: Can view and edit their own facility.
 *  Admin: Can view and delete selected facilities from the admin facility list.
 */
public class ViewFacilityFragment extends Fragment {
    private FragmentViewFacilityBinding binding;
    private User currentUser; // The current logged-in user
    private Facility displayFacility; // The facility being viewed
    private FacilityController controller = new FacilityController();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewFacilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current user from MainActivity
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();

        // Retrieve the facility passed to this fragment
        displayFacility = (Facility) getArguments().getSerializable("facility");

        if (displayFacility != null) {
            // Bind the facility data to the UI
            bindFacilityData(displayFacility);
        } else {
            // Show a toast if no facility data is available
            Toast.makeText(requireContext(), "Facility data unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine the navigation source (previous fragment)
        NavController navController = Navigation.findNavController(view);
        int previousDestinationId = navController.getPreviousBackStackEntry().getDestination().getId();

        // Adjust UI based on navigation source and user role
        if (previousDestinationId == R.id.AdminFragmentFacilityView && currentUser.isAdmin()) {
            // Admin is viewing a facility from the admin facility list
            setupAdminView();
        } else if (previousDestinationId == R.id.HomeFragment && currentUser.isOrganizer()) {
            // Organizer is viewing their own facility
            setupOrganizerView();
        } else {
            // Entrant is viewing the facility
            setupEntrantView();
        }
    }

    /**
     * Binds facility data to the UI elements.
     */
    private void bindFacilityData(Facility facility) {
        binding.textviewFacilityName.setText(String.format("Name: %s", facility.getFacility_name()));
        binding.textviewFacilityLocation.setText(String.format("Location: %s", facility.getFacility_location()));

        if (facility.getposterUri() != null) {
            Uri imageUri = Uri.parse(facility.getposterUri());
            Glide.with(requireContext()).load(imageUri).into(binding.imageviewFacilityPicture);
        }
    }

    /**
     * Configures the UI for an admin.
     */
    private void setupAdminView() {
        binding.buttonDeleteFacility.setVisibility(View.VISIBLE);
        binding.buttonEditFacility.setVisibility(View.GONE);

        // Set delete button click listener
        binding.buttonDeleteFacility.setOnClickListener(v -> deleteFacility());
    }

    /**
     * Configures the UI for an organizer.
     */
    private void setupOrganizerView() {
        binding.buttonEditFacility.setVisibility(View.VISIBLE);
        binding.buttonDeleteFacility.setVisibility(View.GONE);

        // Set up the navigation bar.
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.organizer_nav_menu);
        NavController navController = NavHostFragment.findNavController(this);
        NavController navBarController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        NavigationUI.setupWithNavController(bottomNavigationView, navBarController);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                navController.navigate(R.id.HomeFragment);
                return true;
            }
            else if (item.getItemId() == R.id.nav_add_event) {
                navController.navigate(R.id.EditEventFragment);
                return true;
            }
            else {
                navController.navigate(R.id.EventListFragment);
                return true;
            }
        });

        // Navigate to EditFacilityFragment when edit button is clicked
        binding.buttonEditFacility.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("facility", displayFacility);
            navController.navigate(R.id.action_ViewFacilityFragment_to_EditFacilityFragment, bundle);
        });
    }

    /**
     * Configures the UI for an entrant.
     */
    private void setupEntrantView() {
        binding.buttonEditFacility.setVisibility(View.GONE);
        binding.buttonDeleteFacility.setVisibility(View.GONE);
    }

    /**
     * Deletes the current facility from Firestore and navigates back to the admin facility list.
     */
    private void deleteFacility() {
        controller.deleteFacility(displayFacility.getFacilityId(), new FacilityController.deleteCallback() {
            @Override
            public void onSuccess() {
                // Update the user's isOrganizer field to false
                controller.updateIsOrganizer(currentUser.getDeviceId(), false, new FacilityController.updateCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), "Facility deleted successfully", Toast.LENGTH_SHORT).show();
                        Log.d("ViewFacilityFragment", "Facility and user role updated successfully");

                        // Navigate back to AdminFragment or appropriate view
                        NavController navController = Navigation.findNavController(requireView());
                        navController.popBackStack();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Facility deleted, but failed to update user role", Toast.LENGTH_SHORT).show();
                        Log.e("ViewFacilityFragment", "Error updating user role", e);

                        // Navigate back even if user role update failed
                        NavController navController = Navigation.findNavController(requireView());
                        navController.popBackStack();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to delete facility", Toast.LENGTH_SHORT).show();
                Log.e("ViewFacilityFragment", "Error deleting facility", e);
            }
        });
    }
}