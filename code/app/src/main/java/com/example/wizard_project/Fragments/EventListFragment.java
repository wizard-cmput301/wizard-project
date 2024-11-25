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

import com.example.wizard_project.Adapters.BrowseEventAdapter;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.EventController;
import com.example.wizard_project.Controllers.FacilityController;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEventListBinding;

import java.util.ArrayList;

/**
 * EventListFragment represents a view of the list of all events for a facility.
 */
public class EventListFragment extends Fragment {
    private final ArrayList<Event> eventList = new ArrayList<Event>();
    private EventController eventController;
    private FragmentEventListBinding binding;
    private BrowseEventAdapter adapter;
    private User currentUser;
    private FacilityController facilityController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize components and data
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        eventController = new EventController();
        facilityController = new FacilityController();

        setupListView();
        loadUserFacilityAndEvents();
    }

    /**
     * Sets up the ListView and its adapter.
     */
    private void setupListView() {
        adapter = new BrowseEventAdapter(requireContext(), eventList);
        binding.eventListview.setAdapter(adapter);

        // Set click listener for list items
        binding.eventListview.setOnItemClickListener((adapterView, view, position, id) -> openEventDetails(eventList.get(position)));
    }

    /**
     * Loads the facility associated with the user and fetches its events.
     */
    private void loadUserFacilityAndEvents() {
        String userId = currentUser.getDeviceId();

        // Fetch the user's facility
        facilityController.getFacility(userId, new FacilityController.facilityCallback() {
            @Override
            public void onCallback(Facility facility) {
                if (facility != null) {
                    fetchEventsForFacility(facility.getFacilityId());
                } else {
                    Toast.makeText(requireContext(), "No facility found for the user.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Fetches the list of events for a specific facility.
     *
     * @param facilityId The ID of the facility.
     */
    private void fetchEventsForFacility(String facilityId) {
        eventController.getEventList(facilityId, new EventController.eventCallback() {
            @Override
            public void onCallback(ArrayList<Event> events) {
                if (events != null) {
                    eventList.clear();
                    eventList.addAll(events);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "No events found for this facility.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Opens the details of the selected event.
     *
     * @param selectedEvent The selected Event object.
     */
    private void openEventDetails(Event selectedEvent) {
        NavController navController = NavHostFragment.findNavController(this);
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", selectedEvent);
        navController.navigate(R.id.action_EventListFragment_to_ViewEventFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear binding reference to prevent memory leaks
    }
}
