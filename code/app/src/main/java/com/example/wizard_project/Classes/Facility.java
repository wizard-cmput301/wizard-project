package com.example.wizard_project.Classes;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.List;

/**
 * The Facility class represents a facility in the application, and provides methods to manage facility data.
 * This class is designed to interact with Firestore to store and retrieve facility data.
 */
public class Facility implements Serializable {
    private String facility_name;
    private String facility_location;
    private String userId;
    private String facilityId;
    private String facility_imagePath;
    private String posterUri;
    private List<Event> eventList;

    private FirebaseFirestore db;
    private DocumentReference userRef;

    /**
     * Constructor with parameters
     *
     * @param userId             The Organizer's Device ID.
     * @param facility_name      The name of the facility.
     * @param facilityId         The ID of the facility
     * @param facility_location  The location of the facility.
     * @param facility_imagePath The path to the image of the facility.
     * @param posterUri          The UIR that represents the image of the facility.
     */
    public Facility(String userId, String facilityId, String facility_name, String facility_location, String facility_imagePath, String posterUri) {
        this.userId = userId;
        this.facilityId = facilityId;
        this.facility_name = facility_name;
        this.facility_location = facility_location;
        this.facility_imagePath = facility_imagePath;
        this.posterUri = posterUri;
    }

    /**
     * Constructor without parameters
     */
    public Facility() {
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

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public String getFacilitymagePath() {
        return this.facility_imagePath;
    }

    public void setFacilityImagePath(String facilityimagePath) {
        this.facility_imagePath = facilityimagePath;
    }

    public String getposterUri() {
        return this.posterUri;
    }

    public void setposterUri(String posterUri) {
        this.posterUri = posterUri;
    }

    /**
     * Adds an event to a facility's list of events.
     *
     * @param event The Event object to be added.
     */
    public void addEvent(Event event) {
        eventList.add(event);
    }

    /**
     * Populates the facility object with data from a Firestore document.
     *
     * @param document The Firestore document containing facility data.
     */
    public void setFacilityData(DocumentSnapshot document) {
        this.facilityId = (String) document.get("facilityId");
        this.facility_name = (String) document.get("name");
        this.facility_location = (String) document.get("location");
        this.userId = (String) document.get("userId");
        this.posterUri = (String) document.get("posterUri");
        this.facility_imagePath = (String) document.get("facility_imagePath");
    }
}
