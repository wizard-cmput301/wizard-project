package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.databinding.FragmentOrganizerBinding;
import com.example.wizard_project.databinding.FragmentViewFacilityBinding;

public class ViewFacilityFragment extends Fragment {
    private FragmentViewFacilityBinding binding;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewFacilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        Button editButton = binding.editFacilityButton;
        Button editImageButton = binding.facilityEditImageButton;
        Facility userFacility = currentUser.getFacility();

        binding.facilityViewName.setText(String.format("Facility Name: %s", userFacility.getFacility_name()));
        binding.facilityViewLocation.setText(String.format("Facility Location: %s", userFacility.getFacility_location()));

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
