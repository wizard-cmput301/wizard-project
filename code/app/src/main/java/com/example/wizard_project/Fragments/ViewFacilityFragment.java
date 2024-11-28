package com.example.wizard_project.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * ViewFacilityFragment displays a facility's information, and adjusts the UI based on the user's role:
 * - Entrant: Can view a facility linked to an event.
 * - Organizer: Can view and edit their own facility.
 * - Admin: Can view and delete selected facilities from the admin facility list.
 */
public class ViewFacilityFragment extends Fragment {
    private FragmentViewFacilityBinding binding;
    private User currentUser; // The current logged-in user
    private Facility displayFacility; // The facility being viewed
    private final FacilityController controller = new FacilityController();

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

        // Bind the facility data to the UI
        if (displayFacility != null) {
            bindFacilityData(displayFacility);
            configureViewBasedOnRole(view);
        } else {
            Toast.makeText(requireContext(), "Facility data unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Binds facility data to the UI elements.
     *
     * @param facility The facility being displayed.
     */
    private void bindFacilityData(Facility facility) {
        binding.textviewFacilityName.setText(String.format("Name: %s", facility.getFacility_name()));
        binding.textviewFacilityLocation.setText(String.format("Location: %s", facility.getFacility_location()));

        if (facility.getposterUri() != null) {
            Uri imageUri = Uri.parse(facility.getposterUri());
            Glide.with(requireContext()).load(imageUri).into(binding.imageviewFacilityImage);
        } else {
            binding.imageviewFacilityImage.setImageResource(R.drawable.example_facility); // Default image
        }
    }

    /**
     * Configures the UI based on the user's role and the navigation source.
     *
     * @param view The root view of the fragment.
     */
    private void configureViewBasedOnRole(View view) {
        NavController navController = Navigation.findNavController(view);
        int previousDestinationId = navController.getPreviousBackStackEntry().getDestination().getId();

        if (previousDestinationId == R.id.AdminFragmentFacilityView && currentUser.isAdmin()) {
            setupAdminView();
        } else if (previousDestinationId == R.id.HomeFragment && currentUser.isOrganizer()) {
            setupOrganizerView(navController);
        } else {
            setupEntrantView();
        }
    }

    /**
     * Configures the UI for admins.
     */
    private void setupAdminView() {
        binding.buttonDeleteFacility.setVisibility(View.VISIBLE);
        binding.buttonEditFacility.setVisibility(View.GONE);

        binding.buttonDeleteFacility.setOnClickListener(v -> deleteFacility());
    }

    /**
     * Configures the UI for organizers.
     *
     * @param navController The NavController for navigation.
     */
    private void setupOrganizerView(NavController navController) {
        binding.buttonEditFacility.setVisibility(View.VISIBLE);
        binding.buttonDeleteFacility.setVisibility(View.GONE);

        setupBottomNavigation(navController);

        binding.buttonEditFacility.setOnClickListener(v -> navigateToEditFacility(navController));
    }

    /**
     * Configures the UI for entrants.
     */
    private void setupEntrantView() {
        binding.buttonEditFacility.setVisibility(View.GONE);
        binding.buttonDeleteFacility.setVisibility(View.GONE);
    }

    /**
     * Deletes the current facility and its associated events from Firestore.
     * TODO: delete all events associated with the facility.
     */
    private void deleteFacility() {
        controller.deleteFacilityWithEvents(displayFacility.getFacilityId(), new FacilityController.deleteCallback() {
            @Override
            public void onSuccess() {
                updateIsOrganizerField();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to delete facility and associated events", Toast.LENGTH_SHORT).show();
                Log.e("ViewFacilityFragment", "Error deleting facility and associated events", e);
            }
        });
    }

    /**
     * Updates the isOrganizer field for the current user and navigates back.
     */
    private void updateIsOrganizerField() {
        controller.updateIsOrganizer(currentUser.getDeviceId(), false, new FacilityController.updateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Facility deleted successfully", Toast.LENGTH_SHORT).show();
                NavController navController = Navigation.findNavController(requireView());
                navController.popBackStack();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Facility deleted, but failed to update user role", Toast.LENGTH_SHORT).show();
                Log.e("ViewFacilityFragment", "Error updating user role", e);
                NavController navController = Navigation.findNavController(requireView());
                navController.popBackStack();
            }
        });
    }

    /**
     * Navigates to the EditFacilityFragment to edit the facility.
     *
     * @param navController The NavController for navigation.
     */
    private void navigateToEditFacility(NavController navController) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("facility", displayFacility);
        navController.navigate(R.id.action_ViewFacilityFragment_to_EditFacilityFragment, bundle);
    }

    /**
     * Sets up the bottom navigation menu for organizers.
     *
     * @param navController The NavController for navigation.
     */
    private void setupBottomNavigation(NavController navController) {
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.organizer_nav_menu);

        NavigationUI.setupWithNavController(bottomNavigationView, navController);

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
    }
}
