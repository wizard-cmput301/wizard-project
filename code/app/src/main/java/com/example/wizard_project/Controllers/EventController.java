package com.example.wizard_project.Controllers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EventController acts as a communicator between the database and Event objects.
 * It provides methods to create, retrieve, update, and delete events in the Firestore database.
 */
public class EventController {
    private final FirebaseFirestore db;

    /**
     * Constructs an EventController with a FacilityController and a database instance.
     */
    public EventController() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new event and saves it to the Firestore database.
     *
     * @param newEvent The event to be created.
     * @param userId   The ID of the user creating the event.
     * @param callback The callback to handle success or failure.
     */
    public void createEvent(Event newEvent, String userId, createCallback callback) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event_name", newEvent.getEvent_name());
        eventData.put("event_description", newEvent.getEvent_description());
        eventData.put("event_price", newEvent.getEvent_price());
        eventData.put("event_max_entrants", newEvent.getEvent_max_entrants());
        eventData.put("registration_open", new Timestamp(newEvent.getRegistration_open()));
        eventData.put("registration_close", new Timestamp(newEvent.getRegistration_close()));
        eventData.put("geolocation_requirement", newEvent.isGeolocation_requirement());
        eventData.put("facilityId", newEvent.getFacilityId());
        eventData.put("event_location", newEvent.getEvent_location());
        eventData.put("eventId", newEvent.getEventId());
        eventData.put("posterUri", newEvent.getPosterUri());
        eventData.put("event_image_path", newEvent.getEvent_image_path());

        // Create the event document in the database.
        db.collection("events").document(newEvent.getEventId()).set(eventData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventController", "Event created successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventController", "Error creating event", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Retrieves a list of all events for a specific facility from Firestore.
     *
     * @param facilityId The ID of the facility whose events are to be retrieved.
     * @param callback   A callback interface to handle the retrieved events.
     */
    public void getEventList(String facilityId, eventCallback callback) {
        ArrayList<Event> events = new ArrayList<>();

        db.collection("events").whereEqualTo("facilityId", facilityId).get()
                .addOnSuccessListener(documentSnapshots -> {
                    if (!documentSnapshots.isEmpty()) {
                        for (DocumentSnapshot eventRef : documentSnapshots) {
                            Event event = buildEventFromDocument(eventRef, facilityId);
                            events.add(event);
                        }
                    }
                    callback.onCallback(events);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRetrievalError", "Error retrieving events", e);
                    callback.onCallback(events);
                });
    }

    /**
     * Updates an existing event in the database.
     *
     * @param event    The event to be updated.
     * @param callback The callback to handle success or failure.
     */
    public void updateEvent(Event event, updateCallback callback) {
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Event ID is null or empty"));
            return;
        }

        // Prepare the updated event data
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("event_name", event.getEvent_name());
        updatedData.put("event_description", event.getEvent_description());
        updatedData.put("event_price", event.getEvent_price());
        updatedData.put("event_max_entrants", event.getEvent_max_entrants());
        updatedData.put("registration_open", new Timestamp(event.getRegistration_open()));
        updatedData.put("registration_close", new Timestamp(event.getRegistration_close()));
        updatedData.put("geolocation_requirement", event.isGeolocation_requirement());
        updatedData.put("facilityId", event.getFacilityId());
        updatedData.put("event_location", event.getEvent_location());
        updatedData.put("event_image_path", event.getEvent_image_path());
        updatedData.put("posterUri", event.getPosterUri());

        db.collection("events").document(event.getEventId()).update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventController", "Event updated successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventController", "Error updating event", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Retrieve the list of users in the waiting list for an event.
     *
     * @param eventId    The event whose waiting list is checked.
     * @param callback A callback interface containing the list of waitlisted users.
     */
    public void getWaitingList(String eventId, @NonNull WaitingListCallback callback) {
        db.collection("events").document(eventId).collection("waitingList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Map<String, String>> waitingList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, String> userData = new HashMap<>();
                        userData.put("userId", doc.getString("userId")); // Get device ID
                        userData.put("status", doc.getString("status")); // Get status

                        waitingList.add(userData);
                    }
                    callback.onSuccess(waitingList);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventController", "Failed to fetch waiting list", e);
                    callback.onFailure(e);
                });
    }

    public void setDrawCount(Event event, int drawCount) {
        DocumentReference eventRef = db.collection("events").document(event.getEventId());

        eventRef.update("drawCount", drawCount);
    }

    /**
     * Updates a specific field of a event in the database.
     *
     * @param event  The event to update
     * @param field  The field to update
     * @param update The new value for the field.
     */
    public void updateField(Event event, String field, String update) {
        db.collection("events").document(event.getEventId()).update(field, update)
                .addOnFailureListener(e -> Log.e("FieldUpdateError", "Error updating field", e));
    }

    /**
     * Deletes an event from the database.
     *
     * @param eventId  The ID of the event to delete.
     * @param callback Callback for success or failure.
     */
    public void deleteEvent(String eventId, deleteCallback callback) {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventController", "Event deleted successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventController", "Error deleting event", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Helper method to construct an Event object from a Firestore document.
     *
     * @param document   The Firestore document containing event data.
     * @param facilityId The ID of the facility the event belongs to.
     * @return An Event object populated with the document's data.
     */
    private Event buildEventFromDocument(DocumentSnapshot document, String facilityId) {
        String eventId = document.getString("eventId");
        String eventName = document.getString("event_name");
        String eventDescription = document.getString("event_description");
        int eventPrice = document.getLong("event_price") != null ? document.getLong("event_price").intValue() : 0;
        int maxEntrants = document.getLong("event_max_entrants") != null ? document.getLong("event_max_entrants").intValue() : 0;
        Date registrationOpen = document.getTimestamp("registration_open") != null ? document.getTimestamp("registration_open").toDate() : null;
        Date registrationClose = document.getTimestamp("registration_close") != null ? document.getTimestamp("registration_close").toDate() : null;
        boolean geolocationRequirement = Boolean.TRUE.equals(document.getBoolean("geolocation_requirement"));
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
    }

    // Callback Interfaces
    public interface eventCallback {
        void onCallback(ArrayList<Event> events);
    }

    public interface WaitingListCallback {
        void onSuccess(ArrayList<Map<String, String>> waitingList);
        void onFailure(Exception e);
    }

    public interface deleteCallback {
        void onSuccess();

        void onFailure(Exception e);
    }

    public interface updateCallback {
        void onSuccess();

        void onFailure(Exception e);
    }

    public interface createCallback {
        void onSuccess();

        void onFailure(Exception e);
    }
}
