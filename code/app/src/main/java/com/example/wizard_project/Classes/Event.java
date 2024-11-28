package com.example.wizard_project.Classes;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Representation of an event, created by an organizer and joined by entrants.
 */
public class Event implements Serializable {
    private final String event_location;
    private final String facilityId;
    private String eventId;
    private String event_name;
    private String event_description;
    private int event_price;
    private int event_max_entrants;
    private Date registration_open;
    private Date registration_close;
    private boolean geolocation_requirement;
    private String event_image_path;
    private String posterUri;

    /**
     * Constructor for initializing an Event instance with essential details.
     *
     * @param eventId                 Unique identifier for the event.
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
    public Event(String eventId, String event_name, String event_description, int event_price, int event_max_entrants,
                 Date registration_open, Date registration_close, String facilityId, String event_location,
                 boolean geolocation_requirement, String event_image_path) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString(); // Only generate if it's null
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
}
