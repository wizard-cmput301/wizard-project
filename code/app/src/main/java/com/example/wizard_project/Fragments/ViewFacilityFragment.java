package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.FacilityController;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentViewFacilityBinding;

/**
 * ViewFacilityFragment allows an organizer to view their facility's details.
 */
public class ViewFacilityFragment extends Fragment {
    private FragmentViewFacilityBinding binding;
    private User currentUser;
    private FacilityController controller = new FacilityController();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewFacilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        Facility userFacility = controller.getFacility(currentUser.getDeviceId());
        Button editButton = binding.editFacilityButton;
        Button editImageButton = binding.facilityEditImageButton;

        binding.facilityViewName.setText(String.format("Facility Name: %s", userFacility.getFacility_name()));
        binding.facilityViewLocation.setText(String.format("Facility Location: %s", userFacility.getFacility_location()));

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_ViewFacilityFragment_to_EditFacilityFragment);
            }
        });

        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
