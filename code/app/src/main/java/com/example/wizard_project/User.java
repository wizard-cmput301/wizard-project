package com.example.wizard_project;

public class User {
    private String deviceId;
    private String email;
    private String location;
    private boolean isAdmin;
    private boolean isEntrant;
    private boolean isOrganizer;
    private String name;
    private String phoneNumber;
    private String profilePictureUri;


    // Constructor with all fields
    public User(String deviceId, String email, String location ,boolean isAdmin, boolean isEntrant, boolean isOrganizer, String name, String phoneNumber, String profilePictureUri) {
        this.deviceId = deviceId;
        this.email = email;
        this.location = location;
        this.isAdmin = isAdmin;
        this.isEntrant = isEntrant;
        this.isOrganizer = isOrganizer;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profilePictureUri = profilePictureUri;
    }

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isEntrant() {
        return isEntrant;
    }

    public void setEntrant(boolean entrant) {
        isEntrant = entrant;
    }

    public boolean isOrganizer() {
        return isOrganizer;
    }

    public void setOrganizer(boolean organizer) {
        isOrganizer = organizer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    public void setProfilePictureUri(String profilePictureUri) {
        this.profilePictureUri = profilePictureUri;
    }
}