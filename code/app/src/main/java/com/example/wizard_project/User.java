package com.example.wizard_project;

public class User {
    private String deviceId;
    private String name;
    private String email;
    private String profilePictureUri;

    public User(String deviceId, String name, String email, String profilePictureUri) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.profilePictureUri = profilePictureUri;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    public void setProfilePictureUri(String profilePictureUri) {
        this.profilePictureUri = profilePictureUri;
    }
}
