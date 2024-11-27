package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Adapters.BrowseEventAdapter;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;

/**
 * EntrantEventFragment displays the list of events the user is registered for.
 */
public class EntrantEventFragment extends Fragment {

    private final ArrayList<Event> eventList = new ArrayList<>();
    private BrowseEventAdapter adapter;
    private FirebaseFirestore db;
    private User currentUser;
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up ListView and adapter
        ListView eventListView = view.findViewById(R.id.event_listview);
        adapter = new BrowseEventAdapter(getContext(), eventList);
        eventListView.setAdapter(adapter);

        // Fetch the current user and load registered events
        fetchCurrentUser();

        // Set up navigation to ProfileFragment when the profile picture button is clicked
        View profilePictureButton = requireActivity().findViewById(R.id.profilePictureButton);

        profilePictureButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.ProfileFragment) {
                navController.navigate(R.id.action_EntrantEventFragment_to_ProfileFragment); // (temporary work around, this prevents app crashing when clicking the button twice)
            }
        });


        // Handle ListView item click
        eventListView.setOnItemClickListener((adapterView, itemView, position, id) -> {
            Event selectedEvent = eventList.get(position);
            NavController navController = NavHostFragment.findNavController(this);
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", selectedEvent);
            navController.navigate(R.id.action_EntrantEventFragment_to_EventFragment, bundle);
        });
    }

    /**
     * Loads the events the user is registered for.
     */
    private void loadRegisteredEvents() {
        if (userId == null || userId.isEmpty()) {
            Log.e("EntrantEventFragment", "User ID is null or empty.");
            return;
        }

        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.clear(); // Clear the existing list
                        for (QueryDocumentSnapshot eventDocument : task.getResult()) {
                            String eventId = eventDocument.getId();

                            // Check if user is in the waiting list for this event
                            db.collection("events")
                                    .document(eventId)
                                    .collection("waitingList")
                                    .document(userId)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            // Parse event and add to the list
                                            Event event = parseEventFromDocument(eventDocument);
                                            if (event != null) {
                                                eventList.add(event);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("EntrantEventFragment", "Error checking waiting list: " + e.getMessage(), e));
                        }
                    } else {
                        Log.e("EntrantEventFragment", "Error fetching events", task.getException());
                    }
                });
    }

    /**
     * Fetches the current user from Firestore using the device ID.
     */
    private void fetchCurrentUser() {
        String deviceId = getDeviceId();
        Log.d("EntrantEventFragment", "Device ID: " + deviceId);

        if (deviceId == null || deviceId.isEmpty()) {
            Log.e("EntrantEventFragment", "Device ID is null or empty. Cannot fetch user.");
            return;
        }

        db.collection("users")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            userId = documentSnapshot.getId();
                            Log.d("EntrantEventFragment", "User data fetched: " + currentUser.getName());
                            loadRegisteredEvents(); // Load events the user is registered for
                        } else {
                            Log.e("EntrantEventFragment", "User data is null.");
                        }
                    } else {
                        Log.e("EntrantEventFragment", "No user found with this device ID.");
                        Toast.makeText(getContext(), "No user found with this device ID.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrantEventFragment", "Error fetching user data: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error fetching user data.", Toast.LENGTH_SHORT).show();
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
            String eventId = document.getString("eventId");

            Event event = new Event(
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

    /**
     * Retrieves the device ID.
     */
    private String getDeviceId() {
        try {
            return Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            Log.e("EntrantEventFragment", "Failed to get device ID", e);
            return null;
        }
    }
}
