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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.FacilityController;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEditFacilityBinding;
import com.example.wizard_project.MainActivity;


public class EditFacilityFragment extends Fragment {
    private FragmentEditFacilityBinding binding;
    private User currentUser;
    private final FacilityController controller = new FacilityController();

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
        String userId = currentUser.getDeviceId();
        NavController navController = NavHostFragment.findNavController(this);
        EditText facilityName = binding.facilityEditName;
        EditText facilityLocation = binding.facilityEditLocation;
        Button doneButton = binding.facilityDoneButton;
        Button uploadImageButton = binding.facilityEditImageButton;
        ImageView facilityImage = binding.facilityEditImageview;
        Facility userFacility;


        userFacility = controller.getFacility(userId);
        facilityName.setText(userFacility.getFacility_name());
        facilityLocation.setText(userFacility.getFacility_location());


        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = facilityName.getText().toString();
                String newLocation = facilityLocation.getText().toString();

                if (newName.trim().isEmpty()) {
                    facilityName.setError("Please enter a valid facility name.");

                } else if (newLocation.trim().isEmpty()) {
                    facilityLocation.setError("Please enter a valid location.");

                } else {
                    if (currentUser.isOrganizer()) {
                        userFacility.setFacility_name(newName);
                        userFacility.setFacility_location(newLocation);
                        controller.updateFacility(userFacility);
                        navController.navigate(R.id.action_EditFacilityFragment_to_ViewFacilityFragment);

                    } else {
                        Facility newFacility = controller.createFacility(userId, newName, newLocation);
                        controller.updateFacility(newFacility);
                        currentUser.setOrganizer(true);
                        navController.navigate(R.id.action_EditFacilityFragment_to_ViewFacilityFragment);
                    }
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
