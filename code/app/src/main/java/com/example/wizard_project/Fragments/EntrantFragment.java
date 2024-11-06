package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEntrantBinding;

/**
 * EntrantFragment represents the UI and functionality for entrants
 */
public class EntrantFragment extends Fragment {

    private FragmentEntrantBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntrantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO: temporary, delete when implementing the fragment
        binding.textviewSecond.setText("This is the Entrant Fragment");

        // Get the profile picture button from the activity's toolbar
        View profilePictureButton = requireActivity().findViewById(R.id.profilePictureButton);

        // Set up navigation to ProfileFragment when the profile picture button is clicked
        profilePictureButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.ProfileFragment) {
                navController.navigate(R.id.action_EntrantFragment_to_ProfileFragment); // (temporary work around, this prevents app crashing when clicking the button twice)
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}