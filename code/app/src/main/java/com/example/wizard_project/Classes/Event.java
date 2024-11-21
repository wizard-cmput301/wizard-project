package com.example.wizard_project.Classes;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Representation of an event, created by an organizer and joined by entrants.
 */
public class Event implements Serializable {
    private String event_name;
    private String event_location;
    private int event_price;
    private int event_waitlist_limit;
    private Date event_deadline;
    private String facilityId;
    private List<User> entrant_list;
    private List<User> waitlist;
    private String posterUri;
    private String eventId;

    private int daysRemaining;


    /**
     * Constructor for initializing an Event instance with essential details.
     * @param event_name Name of the event.
     * @param event_price Cost to enter the event.
     * @param event_waitlist_limit Maximum number of users on the waitlist.
     * @param event_deadline Deadline date to join the event.
     * @param facilityId ID of the facility where the event is held.
     */
    public Event(String event_name, int event_price, int event_waitlist_limit, Date event_deadline, String facilityId, String event_location) {
        this.event_name = event_name;
        this.event_price = event_price;
        this.event_waitlist_limit = event_waitlist_limit;
        this.event_deadline = event_deadline;
        this.facilityId = facilityId;
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString(); // Only generate if it's null
        this.waitlist = new ArrayList<>(); // Initialize the waitlist
        this.event_location = event_location;
        this.eventId = UUID.randomUUID().toString();
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public String getEvent_location() {
        return event_location;
    }

    public void setEvent_location(String event_location) {
        this.event_location = event_location;
    }

    public int getEvent_waitlist_limit() {
        return event_waitlist_limit;
    }

    public void setEvent_waitlist(int event_waitlist_limit) {
        this.event_waitlist_limit = event_waitlist_limit;
    }

    public Date getEvent_deadline() {
        return event_deadline;
    }

    public void setEvent_deadline(Date event_deadline) {
        this.event_deadline = event_deadline;
    }

    public int getEvent_price() {
        return event_price;
    }

    public void setEvent_price(int event_price) {
        this.event_price = event_price;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public String getPosterUri() {
        return posterUri;
    }

    public void setPosterUri(String posterUri) {
        this.posterUri = posterUri;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEvent_waitlist_limit(int event_waitlist_limit) {
        this.event_waitlist_limit = event_waitlist_limit;
    }

    /**
     * Retrieves the number of remaining spots in the waitlist.
     * @return An integer representing how many spots are left in the waitlist.
     */
    public int getRemainingWaitlistLimit() {
        return event_waitlist_limit - waitlist.size();
    }

    /**
     * Add a user to the list of entrants.
     * @param user The user joining the event as an entrant.
     */
    public void addEntrant(User user) {
        entrant_list.add(user);
    }

    /**
     * Add a user to the waitlist.
     * @param user The user to be added to the waitlist.
     */
    public void addToWaitlist(User user) {
        waitlist.add(user);
    }

    /**
     * Clears the event's data in memory, setting fields to default or empty values.
     * TODO: This currently isn't being used because it was causing a crash, not sure if needed
     */
    public void removeFacilityDataMemory() {
        this.event_name = "";
        this.event_price = 0;
        this.event_waitlist_limit = 0;
        this.event_deadline = null;
        this.facilityId = "";
        this.eventId = "";
        this.waitlist = null;
    }

    /**
     * Populates the Event object with data from a Firestore document.
     * @param document The Firestore document containing event data.
     */
    public void setEventData(DocumentSnapshot document) {
        this.event_location = (String) document.get("location");
        this.eventId = document.getId(); // Assigns Firestore's document ID to eventId
        this.event_name = document.getString("name");
        this.facilityId = document.getString("facilityId");

        Long priceValue = document.getLong("price");
        this.event_price = (priceValue != null) ? priceValue.intValue() : 0;

        Long waitlistLimitValue = document.getLong("waitlist_limit");
        this.event_waitlist_limit = (waitlistLimitValue != null) ? waitlistLimitValue.intValue() : 0;

        Timestamp eventEndTime = document.getTimestamp("end_time");
        this.event_deadline = (eventEndTime != null) ? eventEndTime.toDate() : null;
    }
}
