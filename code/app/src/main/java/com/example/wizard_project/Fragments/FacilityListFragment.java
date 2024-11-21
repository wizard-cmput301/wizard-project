package com.example.wizard_project.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
    private FacilityController facilityController;
    private FragmentFacilityListBinding binding;
    private final ArrayList<Facility> facilityList = new ArrayList<>();
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

        // Initialize variables
        MainActivity mainActivity = (MainActivity) requireActivity();
        NavController navController = NavHostFragment.findNavController(this);
        currentUser = mainActivity.getCurrentUser();
        ListView facilityListView = binding.facilityListview;

        // Set up the ListView adapter
        adapter = new BrowseFacilityAdapter(getContext(), facilityList);
        facilityListView.setAdapter(adapter);

        // Initialize the FacilityController and load facilities
        facilityController = new FacilityController();
        loadFacilities();

        // Handle item click events to navigate to the facility detail view
        binding.facilityListview.setOnItemClickListener((adapterView, itemView, position, id) -> {
            Facility selectedFacility = facilityList.get(position);
            Bundle bundle = new Bundle();
            bundle.putSerializable("facility", selectedFacility);
            navController.navigate(R.id.action_AdminFragmentFacilityView_to_FacilityFragment, bundle);
        });
    }

    /**
     * Loads all facilities from Firestore and updates the facility list.
     */
    private void loadFacilities() {
        facilityController.getFacilities(new FacilityController.facilitiesCallback() {
            @Override
            public void onCallback(ArrayList<Facility> facilities) {
                facilityList.clear();
                facilityList.addAll(facilities); // Update the facility list
                adapter.notifyDataSetChanged(); // Refresh the ListView
            }
        });
    }
}