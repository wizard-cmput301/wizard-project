package com.example.wizard_project.Classes;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Representation of an event, created by an organizer and joined by entrants.
 */

public class Event implements Serializable {
    private String event_name;
    private Facility event_location;
    private int event_price;
    private int event_waitlist_limit;
    private Date event_deadline;
    private String facilityId;
    private List<User> entrant_list;
    private List<User> waitlist;
    private String posterUri;
    private String eventId;



    /**
     * Constructor containing the details of the event.
     * @param event_name Name of the event.
     * @param event_price Cost to enter the event.
     * @param event_waitlist_limit Waitlist limit for the event.
     * @param event_deadline Date representing the deadline to join the event.
     */
    public Event(String event_name, int event_price, int event_waitlist_limit, Date event_deadline, String facilityId) {
        this.event_name = event_name;
        this.event_price = event_price;
        this.event_waitlist_limit = event_waitlist_limit;
        this.event_deadline = event_deadline;
        this.facilityId = facilityId;
        this.eventId = UUID.randomUUID().toString();
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public Facility getEvent_location() {
        return event_location;
    }

    public void setEvent_location(Facility event_location) {
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
     * @param user The user to be added.
     */
    public void addEntrant(User user) {
        entrant_list.add(user);
    }

    /**
     * Add a user to the waitlist.
     * @param user The user to be added.
     */
    public void addToWaitlist(User user) {
        waitlist.add(user);
    }

    /**
     * Populates the Event object with data from a Firestore document.
     *
     * @param document The Firestore document containing event data.
     */
    public void setEventData(DocumentSnapshot document) {
        this.event_name = (String) document.get("name");
        this.eventId = (String) document.get("eventId");
        this.facilityId = (String) document.get(facilityId);

        Long priceValue = document.getLong("price");
        this.event_price = (priceValue != null) ? priceValue.intValue() : 0;

        Long waitlistLimitValue = document.getLong("waitlist_limit");
        this.event_waitlist_limit = (waitlistLimitValue != null) ? waitlistLimitValue.intValue() : 0;

        Timestamp eventEndTime = document.getTimestamp("end_time");
        this.event_deadline = (eventEndTime != null) ? eventEndTime.toDate() : null;
    }

}
