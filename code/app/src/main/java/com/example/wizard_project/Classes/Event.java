package com.example.wizard_project.Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Representation of an event, created by an organizer and joined by entrants.
 */
public class Event implements Serializable {
    private String event_name;
    private String event_description;
    private int event_price;
    private int event_max_entrants;
    private boolean geolocation_requirement;
    private Date registration_open;
    private Date registration_close;
    private final String event_location;
    private String event_image_path;
    private final String facilityId;
    private final List<User> entrant_list;
    private final List<User> waitlist;
    private String posterUri;
    private String eventId;

    /**
     * Constructor for initializing an Event instance with essential details.
     *
     * @param event_name              Name of the event.
     * @param event_description       Description of the event.
     * @param event_price             Cost to enter the event.
     * @param event_max_entrants      Maximum number of entrants.
     * @param registration_open       Date when registration opens.
     * @param registration_close      Date when registration closes.
     * @param facilityId              ID of the facility where the event is held.
     * @param event_location          Location of the event (facility location).
     * @param geolocation_requirement Whether geolocation is required for the event.
     * @param event_image_path        Path to the facility's image.
     */
    public Event(String event_name, String event_description, int event_price, int event_max_entrants, Date registration_open,
                 Date registration_close, String facilityId, String event_location, boolean geolocation_requirement, String event_image_path) {
        this.event_name = event_name;
        this.event_description = event_description;
        this.event_price = event_price;
        this.event_max_entrants = event_max_entrants;
        this.geolocation_requirement = geolocation_requirement;
        this.registration_open = registration_open;
        this.registration_close = registration_close;
        this.facilityId = facilityId;
        this.event_image_path = event_image_path;
        this.event_location = event_location;
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString(); // Only generate if it's null
        this.waitlist = new ArrayList<>();
        this.entrant_list = new ArrayList<>();
        this.posterUri = null; // Initialize to null by default
    }

    // Getters and setters
    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public String getEvent_description() {
        return event_description;
    }

    public void setEvent_description(String event_description) {
        this.event_description = event_description;
    }

    public int getEvent_price() {
        return event_price;
    }

    public void setEvent_price(int event_price) {
        this.event_price = event_price;
    }

    public int getEvent_max_entrants() {
        return event_max_entrants;
    }

    public void setEvent_max_entrants(int event_max_entrants) {
        this.event_max_entrants = event_max_entrants;
    }

    public boolean isGeolocation_requirement() {
        return geolocation_requirement;
    }

    public void setGeolocation_requirement(boolean geolocation_requirement) {
        this.geolocation_requirement = geolocation_requirement;
    }

    public Date getRegistration_open() {
        return registration_open;
    }

    public void setRegistration_open(Date registration_open) {
        this.registration_open = registration_open;
    }

    public Date getRegistration_close() {
        return registration_close;
    }

    public void setRegistration_close(Date registration_close) {
        this.registration_close = registration_close;

    }

    public String getFacilityId() {
        return facilityId;
    }

    public String getEvent_image_path() {
        return this.event_image_path;
    }

    public void setEvent_image_path(String eventImagePath) {
        this.event_image_path = eventImagePath;
    }

    public String getEvent_location() {
        return event_location;
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

    /**
     * Retrieves the number of remaining spots in the waitlist.
     *
     * @return An integer representing how many spots are left in the waitlist.
     */
    public int getRemainingWaitlistLimit() {
        return event_max_entrants - waitlist.size();
    }

    /**
     * Add a user to the list of entrants.
     *
     * @param user The user joining the event as an entrant.
     */
    public void addEntrant(User user) {
        entrant_list.add(user);
    }

    /**
     * Add a user to the waitlist.
     *
     * @param user The user to be added to the waitlist.
     */
    public void addToWaitlist(User user) {
        waitlist.add(user);
    }
}
