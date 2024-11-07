package com.example.wizard_project.Classes;

import java.util.List;

public class Facility {
    private String facility_name;
    private String facility_location;
    private List<Event> eventList;

    public Facility(String facility_name, String facility_location) {
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

    public void addEvent(Event event) {
        eventList.add(event);
    }
}
