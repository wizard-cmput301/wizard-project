package com.example.wizard_project.Fragments;
import android.os.Bundle;
import android.util.Log;
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
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.FacilityController;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEditFacilityBinding;
import com.example.wizard_project.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * EditFacilityFragment allows an organizer to create or edit a facility.
 */
public class EditFacilityFragment extends Fragment {
    private FragmentEditFacilityBinding binding;
    private User currentUser;
    private Facility userFacility;
    private final FacilityController controller = new FacilityController();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditFacilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        String userId = currentUser.getDeviceId();
        EditText facilityName = binding.facilityEditName;
        EditText facilityLocation = binding.facilityEditLocation;
        Button doneButton = binding.facilityDoneButton;
        Button uploadImageButton = binding.facilityEditImageButton;
        ImageView facilityImage = binding.facilityEditImageview;


        // Populate the text fields with existing facility info.
        controller.getFacility(userId, new FacilityController.facilityCallback() {
            @Override
            public void onCallback(Facility facility) {
                if (facility != null) {
                    userFacility = facility;
                    facilityName.setText(userFacility.getFacility_name());
                    facilityLocation.setText(userFacility.getFacility_location());
                }
            }
        });

        // Submit the inputted facility info, direct user to facility view.
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
                        // Update existing facility info if the user is already an organizer.
                        userFacility.setFacility_name(newName);
                        userFacility.setFacility_location(newLocation);
                        controller.updateFacility(userFacility);
                        navController.navigate(R.id.action_EditFacilityFragment_to_ViewFacilityFragment);

                    } else {
                        // Create new facility with the inputted info.
                        Facility newFacility = controller.createFacility(userId, newName, newLocation);
                        controller.updateFacility(newFacility);
                        currentUser.setOrganizer(true);
                        navController.navigate(R.id.action_EditFacilityFragment_to_ViewFacilityFragment);
                    }
                }
            }
        });

        // Prompt the user to select a photo and add it to the database.
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
