package com.example.wizard_project.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
 * ViewFacilityFragment allows an organizer to view their facility's details.
 */
public class ViewFacilityFragment extends Fragment {
    private FragmentViewFacilityBinding binding;
    private User currentUser;
    private Facility userFacility;
    private FacilityController controller = new FacilityController();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewFacilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        String userId = currentUser.getDeviceId();
        Button editButton = binding.editFacilityButton;
        Button editImageButton = binding.facilityEditImageButton;

        // Populate the facility attribute fields with the facility info from the database.
        controller.getFacility(userId, new FacilityController.facilityCallback() {
            @Override
            public void onCallback(Facility facility) {
                if (facility != null) {
                    userFacility = facility;
                    binding.facilityViewName.setText(String.format("Facility Name: %s", userFacility.getFacility_name()));
                    binding.facilityViewLocation.setText(String.format("Facility Location: %s", userFacility.getFacility_location()));
                    if(userFacility.getFacilitymagePath() != null){
                        Uri imageUri = Uri.parse(userFacility.getposterUri());
                        Glide.with(requireContext()).load(imageUri).into(binding.facilityViewImageview);
                    }
                }
            }
        });

        // Open the facility editor.
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_ViewFacilityFragment_to_EditFacilityFragment);
            }
        });

        // Prompt the user to upload a photo and add it to the database.
        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
