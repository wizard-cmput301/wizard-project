package com.example.wizard_project.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Adapters.BrowseFacilityAdapter;
import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.FacilityController;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentFacilityListBinding;

import java.util.ArrayList;

/**
 * FacilityListFragment allows the admin to browse facilities and manage them.
 */
public class FacilityListFragment extends Fragment {
    private final ArrayList<Facility> facilityList = new ArrayList<>();
    private FacilityController facilityController;
    private FragmentFacilityListBinding binding;
    private BrowseFacilityAdapter adapter;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFacilityListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize components and user data
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        facilityController = new FacilityController();

        setupListView();
        loadFacilities();
    }

    /**
     * Sets up the ListView and its adapter, and handles item clicks.
     */
    private void setupListView() {
        adapter = new BrowseFacilityAdapter(requireContext(), facilityList);
        binding.facilityListview.setAdapter(adapter);

        // Handle ListView item clicks
        binding.facilityListview.setOnItemClickListener((adapterView, itemView, position, id) -> {
            Facility selectedFacility = facilityList.get(position);
            openFacilityDetails(selectedFacility);
        });
    }

    /**
     * Loads facilities from Firestore and updates the ListView.
     */
    private void loadFacilities() {
        facilityController.getFacilities(new FacilityController.facilitiesCallback() {
            @Override
            public void onCallback(ArrayList<Facility> facilities) {
                if (facilities != null && !facilities.isEmpty()) {
                    facilityList.clear();
                    facilityList.addAll(facilities);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "No facilities available.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Navigates to the FacilityFragment to view details of the selected facility.
     *
     * @param selectedFacility The facility selected by the user.
     */
    private void openFacilityDetails(Facility selectedFacility) {
        NavController navController = NavHostFragment.findNavController(this);
        Bundle bundle = new Bundle();
        bundle.putSerializable("facility", selectedFacility);
        navController.navigate(R.id.action_AdminFragmentFacilityView_to_FacilityFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear binding to prevent memory leaks
    }
}
