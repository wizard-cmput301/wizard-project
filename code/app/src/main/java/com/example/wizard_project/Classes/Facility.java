package com.example.wizard_project.Classes;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.UUID;

public class Facility {
    private String facility_name;
    private String facility_location;
    private String userId;
    private String facilityId;
    private List<Event> eventList;

    private FirebaseFirestore db;
    private DocumentReference userRef;

    public Facility(String userId, String facility_name, String facility_location) {
        this.userId = userId;
        this.facilityId = UUID.randomUUID().toString();
        this.facility_name = facility_name;
        this.facility_location = facility_location;
    }

    public String getFacility_name() {
        return facility_name;
    }

    public void setFacility_name(String facility_name) {
        this.facility_name = facility_name;
    }

    public String getFacility_location() {
        return facility_location;
    }

    public void setFacility_location(String facility_location) {
        this.facility_location = facility_location;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void addEvent(Event event) {
        eventList.add(event);
    }


    /**
     * Populates the facility object with data from a Firestore document.
     *
     * @param document The Firestore document containing facility data.
     */
    public void setUserData(DocumentSnapshot document) {
        this.facilityId = (String) document.get("facilityId");
        this.facility_name = (String) document.get("name");
        this.facility_location = (String) document.get("location");
        this.userId = (String) document.get("userId");
    }
}
