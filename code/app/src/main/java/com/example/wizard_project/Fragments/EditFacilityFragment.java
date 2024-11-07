package com.example.wizard_project.Fragments;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEditFacilityBinding;
import com.example.wizard_project.MainActivity;


public class EditFacilityFragment extends Fragment {
    private FragmentEditFacilityBinding binding;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditFacilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        NavController navController = NavHostFragment.findNavController(this);

        EditText facilityName = binding.facilityEditName;
        EditText facilityLocation = binding.facilityEditLocation;
        Button doneButton = binding.facilityDoneButton;
        Button uploadImageButton = binding.facilityEditImageButton;
        ImageView facilityImage = binding.facilityEditImageview;
        Facility userFacility;

        if (currentUser.isOrganizer()) {
            userFacility = currentUser.getFacility();
            facilityName.setText(userFacility.getFacility_name());
            facilityLocation.setText(userFacility.getFacility_location());
        }
        else {
            userFacility = null;
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = facilityName.getText().toString();
                String newLocation = facilityLocation.getText().toString();

                if (userFacility != null) {
                    userFacility.setFacility_name(newName);
                    userFacility.setFacility_location(newLocation);
                }

                else {
                    Facility newFacility = new Facility(newName, newLocation);
                    currentUser.setFacility(newFacility);
                    currentUser.setOrganizer(true);
                    navController.navigate(R.id.action_EditFacilityFragment_to_ViewFacilityFragment);
                }
            }
        });

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
