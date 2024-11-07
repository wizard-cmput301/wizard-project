package com.example.wizard_project.Classes;

import java.io.Serializable;

public class Event implements Serializable {
    private String event_name;
    private Facility event_location;
    private int event_price;
    private int event_waitlist;
    private int event_deadline;

    public Event(String event_name, int event_price, int event_waitlist, int event_deadline) {
        this.event_name = event_name;
        this.event_price = event_price;
        this.event_waitlist = event_waitlist;
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

    public int getEvent_waitlist() {
        return event_waitlist;
    }

    public void setEvent_waitlist(int event_waitlist) {
        this.event_waitlist = event_waitlist;
    }

    public int getEvent_deadline() {
        return event_deadline;
    }

    public void setEvent_deadline(int event_deadline) {
        this.event_deadline = event_deadline;
    }

    public int getEvent_price() {
        return event_price;
    }

    public void setEvent_price(int event_price) {
        this.event_price = event_price;
    }
}
