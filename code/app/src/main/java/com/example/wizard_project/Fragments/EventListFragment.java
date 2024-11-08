package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
import java.util.List;

/**
 * EventListFragment represents a view of the list of all events for a facility.
 */
public class EventListFragment extends Fragment {
    private EventController eventController;
    private FragmentEventListBinding binding;
    private ArrayList<Event> eventList = new ArrayList<Event>();
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

        MainActivity mainActivity = (MainActivity) requireActivity();
        NavController navController = NavHostFragment.findNavController(this);
        currentUser = mainActivity.getCurrentUser();
        String userId = currentUser.getDeviceId();
        ListView eventListView = binding.eventListview;
        adapter = new BrowseEventAdapter(getContext(), eventList);
        eventController = new EventController();
        facilityController = new FacilityController();
        eventListView.setAdapter(adapter);

        // Get the facility associated with the user.
        facilityController.getFacility(userId, new FacilityController.facilityCallback() {
            @Override
            public void onCallback(Facility facility) {
                // Get the list of events for the facility.
                eventController.getEventList(facility.getFacilityId(), new EventController.eventCallback() {

                    @Override
                    public void onCallback(ArrayList<Event> events) {
                        // For each event, add it to the list to be displayed.
                        eventList.clear();
                        eventList.addAll(events);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        // Set the click listener for each item in the ListView.
        binding.eventListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Open the event viewer when an item is clicked.
                Event selectedEvent = eventList.get(i);
                Bundle bundle = new Bundle();
                bundle.putSerializable("event", selectedEvent);
                navController.navigate(R.id.action_EventListFragment_to_ViewEventFragment, bundle);
            }
        });





    }
}
