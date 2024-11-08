package com.example.wizard_project.Controllers;

import android.util.Log;

import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EventController acts as a communicator between the database and Event objects.
 * Facilitates the addition, retrieval, and updating of Event objects.
 */
public class EventController {
    private FirebaseFirestore db;
    private FacilityController facilityController;

    /**
     * Constructs an EventController with a FacilityController and a database instance.
     */
    public EventController() {
        this.db = FirebaseFirestore.getInstance();
        this.facilityController = new FacilityController();
    }

    /**
     * Creates a new event with the provided attributes and adds it to the database.
     * @param facilityId The ID of the facility the event is held at.
     * @param event_name The name of the event.
     * @param event_price The cost to enter the event.
     * @param event_waitlist_limit The number of entrants that can sign up for the waitlist.
     * @param eventDeadline The deadline for entrants to sign up for the event.
     * @param eventLocation The location of the event; the facility the event is held at.
     * @return The newly created Event object.
     */
    public Event createEvent(String facilityId, String event_name, int event_price, int event_waitlist_limit, Date eventDeadline, String eventLocation) {
        // Create a new Event object with the attributes.
        Event newEvent = new Event(event_name, event_price, event_waitlist_limit, eventDeadline, facilityId, eventLocation);

        // Create a new hashmap with each of the event's attributes and their corresponding values.
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", event_name);
        eventData.put("price", event_price);
        eventData.put("waitlist_limit", event_waitlist_limit);
        eventData.put("end_time", eventDeadline);
        eventData.put("facilityId", facilityId);
        eventData.put("location", eventLocation);
        eventData.put("eventId", newEvent.getEventId());

        // Create the event document in the database.
        db.collection("events").document(newEvent.getEventId()).set(eventData)
                .addOnSuccessListener(aVoid -> Log.d("EventCreated", "Successfully added event."))
                .addOnFailureListener(e -> { Log.e("EventError", "Failed to create event.", e); });
        return newEvent;
    }

    /**
     * Get a list of all events for a facility.
     * @param facilityId The ID of the facility whose events are retrieved.
     * @param callback A callback interface containing the list of retrieved events.
     */
    public void getEventList(String facilityId, eventCallback callback) {
        // Initialize a new ArrayList.
        ArrayList<Event> events = new ArrayList<>();

        // Search for all events with a matching facilityId, add them to the list, and return the list through the callback.
        db.collection("events").whereEqualTo("facilityId", facilityId).get()
                .addOnSuccessListener(documentSnapshots -> {
                    if (!documentSnapshots.isEmpty()) {
                        for(int i = 0; i < documentSnapshots.size(); i++) {
                            DocumentSnapshot eventRef = documentSnapshots.getDocuments().get(i);
                            Event newEvent = new Event("", 0, 0, null, facilityId, "");
                            newEvent.setEventData(eventRef);
                            events.add(newEvent);
                        }
                        callback.onCallback(events);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRetrievalError", "Error retrieving events: ", e);
                    callback.onCallback(events);
                });
    }

    /**
     * Update an event's details.
     * @param event The event whose details are updated.
     */
    public void updateEvent(Event event) {
        // Retrieve the event document.
        DocumentReference eventRef = db.collection("events").document(event.getEventId());

        // Update each attribute of the event in the database.
        eventRef.update("name", event.getEvent_name());
        eventRef.update("price", event.getEvent_price());
        eventRef.update("waitlist_limit", event.getEvent_waitlist_limit());
        eventRef.update("facilityId", event.getFacilityId());
        eventRef.update("end_time", new Timestamp(event.getEvent_deadline()));
    }

    /**
     * Retrieve the list of users in the waiting list for an event.
     * @param event The event whose waiting list is checked.
     * @param callback A callback interface containing the list of waitlisted users.
     */
    public void getWaitlistEntrants(Event event, waitListCallback callback) {
        ArrayList<User> waitingList = new ArrayList<>();

        db.collection("events").document(event.getEventId()).collection("waitingList").get()
                .addOnSuccessListener(documentSnapshots -> {
                    if (!documentSnapshots.isEmpty()) {
                        for (int i = 0; i < documentSnapshots.size(); i++) {
                            DocumentSnapshot userRef = documentSnapshots.getDocuments().get(i);
                            User newUser = new User();
                            newUser.setUserData(userRef);
                            waitingList.add(newUser);
                        }
                        callback.onCallback(waitingList);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRetrievalError", "Error retrieving waitlisted users: ", e);
                    callback.onCallback(waitingList);
                });
    }

    /**
     * An interface that facilitates the retrieval of events for a given facility from the database.
     */
    public interface eventCallback {
        void onCallback(ArrayList<Event> events);
    }

    /**
     * An interface that facilitates the retrieval of waitlisted users for an event from the database.
     */
    public interface waitListCallback {
        void onCallback(ArrayList<User> users);
    }

}
