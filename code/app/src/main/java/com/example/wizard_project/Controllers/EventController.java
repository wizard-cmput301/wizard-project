package com.example.wizard_project.Controllers;

import android.util.Log;

import com.example.wizard_project.Classes.Event;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventController {
    private FirebaseFirestore db;
    private FacilityController facilityController;

    public EventController() {
        this.db = FirebaseFirestore.getInstance();
        this.facilityController = new FacilityController();
    }

    public Event createEvent(String facilityId, String event_name, int event_price, int event_waitlist_limit, Date eventDeadline) {
        Event newEvent = new Event(event_name, event_price, event_waitlist_limit, eventDeadline, facilityId);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", event_name);
        eventData.put("price", event_price);
        eventData.put("waitlist_limit", event_waitlist_limit);
        eventData.put("end_time", eventDeadline);
        eventData.put("facilityId", facilityId);

        db.collection("events").document(newEvent.getEventId()).set(eventData)
                .addOnSuccessListener(aVoid -> Log.d("EventCreated", "Successfully added event."))
                .addOnFailureListener(e -> { Log.e("EventError", "Failed to create event.", e); });
        return newEvent;
    }

    public void getEventList(String facilityId, eventCallback callback) {
        ArrayList<Event> events = new ArrayList<>();

        db.collection("events").whereEqualTo("facilityId", facilityId).get()
                .addOnSuccessListener(documentSnapshots -> {
                    if (!documentSnapshots.isEmpty()) {
                        for(int i = 0; i < documentSnapshots.size(); i++) {
                            DocumentSnapshot eventRef = documentSnapshots.getDocuments().get(i);
                            Event newEvent = new Event("", 0, 0, null, facilityId);
                            newEvent.setEventData(eventRef);
                            events.add(newEvent);
                        }
                        callback.onCallback(events);
                    }
                    else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRetrievalError", "Error retrieving events: ", e);
                    callback.onCallback(null);
                });
    }

    public void updateEvent(Event event) {
        DocumentReference eventRef = db.collection("events").document(event.getEventId());

        eventRef.update("name", event.getEvent_name());
        eventRef.update("price", event.getEvent_price());
        eventRef.update("waitlist_limit", event.getEvent_waitlist_limit());
        eventRef.update("facilityId", event.getFacilityId());
        eventRef.update("end_time", new Timestamp(event.getEvent_deadline()));
    }

    public interface eventCallback {
        void onCallback(ArrayList<Event> events);
    }


}
