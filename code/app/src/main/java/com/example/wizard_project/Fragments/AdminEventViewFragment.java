package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Adapters.BrowseEventAdapter;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEventListBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;

/**
 * AdminEventViewFragment displays a list of all events for the admin user,
 * allowing navigation to event details.
 */
public class AdminEventViewFragment extends Fragment {
    private final ArrayList<Event> eventList = new ArrayList<>();
    private FragmentEventListBinding binding;
    private BrowseEventAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize NavController for navigation
        NavController navController = NavHostFragment.findNavController(this);

        // Set up the ListView and adapter
        ListView eventListView = binding.eventListview;
        adapter = new BrowseEventAdapter(getContext(), eventList);
        eventListView.setAdapter(adapter);

        // Load all events from the database
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
                            // Parse Firestore document to Event object
                            Event event = parseEventFromDocument(document);
                            if (event != null) {
                                eventList.add(event);
                            }
                        }
                        adapter.notifyDataSetChanged(); // Refresh the ListView with new data
                    } else {
                        Log.e("FirestoreError", "Error loading events", task.getException());
                    }
                });
    }

    /**
     * Parses a Firestore document into an Event object.
     *
     * @param document The Firestore document containing event data.
     * @return A populated Event object or null if parsing fails.
     */
    private Event parseEventFromDocument(QueryDocumentSnapshot document) {
        try {
            String eventId = document.getString("eventId");
            String eventName = document.getString("event_name");
            String eventDescription = document.getString("event_description");
            int eventPrice = document.contains("event_price") ? document.getLong("event_price").intValue() : 0;
            int maxEntrants = document.contains("event_max_entrants") ? document.getLong("event_max_entrants").intValue() : 0;
            Date registrationOpen = document.contains("registration_open") ? document.getTimestamp("registration_open").toDate() : null;
            Date registrationClose = document.contains("registration_close") ? document.getTimestamp("registration_close").toDate() : null;
            boolean geolocationRequirement = Boolean.TRUE.equals(document.getBoolean("geolocation_requirement"));
            String facilityId = document.getString("facilityId");
            String eventLocation = document.getString("event_location");
            String eventImagePath = document.getString("event_image_path");
            String posterUri = document.getString("posterUri");

            Event event = new Event(
                    eventId,
                    eventName,
                    eventDescription,
                    eventPrice,
                    maxEntrants,
                    registrationOpen,
                    registrationClose,
                    facilityId,
                    eventLocation,
                    geolocationRequirement,
                    eventImagePath
            );
            event.setPosterUri(posterUri);
            event.setEventId(eventId);

            return event;
        } catch (Exception e) {
            Log.e("ParseError", "Error parsing event document", e);
            return null;
        }
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
