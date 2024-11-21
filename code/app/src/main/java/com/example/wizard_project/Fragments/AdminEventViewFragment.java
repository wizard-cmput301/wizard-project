package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wizard_project.Adapters.BrowseEventAdapter;
import com.example.wizard_project.Adapters.BrowseProfileAdapter;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.EventController;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.databinding.FragmentAdminBinding;
import com.example.wizard_project.databinding.FragmentEventListBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * AdminEventViewFragment displays a list of all events for the admin user,
 * allowing navigation to event details.
 */
public class AdminEventViewFragment extends Fragment {
    private FragmentEventListBinding binding;
    private ArrayList<Event> eventList = new ArrayList<>();
    private BrowseEventAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);
        ListView eventListView = binding.eventListview;
        adapter = new BrowseEventAdapter(getContext(), eventList);
        eventListView.setAdapter(adapter);

        // Load all events for the admin view
        loadAllEvents();

        // Handle item clicks to navigate to event details
        binding.eventListview.setOnItemClickListener((adapterView, itemView, position, id) -> {
            Event selectedEvent = eventList.get(position);
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", selectedEvent);
            navController.navigate(R.id.action_AdminFragmentEventView_to_EventFragment, bundle);
        });
    }

    /**
     * Loads all events from Firestore and updates the event list.
     */
    private void loadAllEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.clear(); // Clear existing events before loading new ones
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = new Event(null, 0, 0, null, null, null);
                            event.setEventData(document);
                            eventList.add(event); // Add each event to the list
                        }
                        adapter.notifyDataSetChanged(); // Refresh the ListView with new data
                    } else {
                        // TODO: Error handling
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllEvents(); // Refresh the event list when returning to this fragment
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clean up binding reference
    }
}
