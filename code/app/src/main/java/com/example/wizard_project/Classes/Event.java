package com.example.wizard_project.Classes;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Representation of an event, created by an organizer and joined by entrants.
 */

public class Event implements Serializable {
    private String event_name;
    private Facility event_location;
    private int event_price;
    private int event_waitlist_limit;
    private Date event_deadline;
    private List<User> entrant_list;
    private List<User> waitlist;


    /**
     * Constructor containing the details of the event.
     * @param event_name Name of the event.
     * @param event_price Cost to enter the event.
     * @param event_waitlist_limit Waitlist limit for the event.
     * @param event_deadline Date representing the deadline to join the event.
     */
    public Event(String event_name, int event_price, int event_waitlist_limit, Date event_deadline) {
        this.event_name = event_name;
        this.event_price = event_price;
        this.event_waitlist_limit = event_waitlist_limit;
        this.event_deadline = event_deadline;
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
}
