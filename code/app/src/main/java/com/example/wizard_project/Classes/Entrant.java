package com.example.wizard_project.Classes;

public class Entrant {
    private String name;
    private String status;
    private String userId;
    private Double latitude;
    private Double longitude;

    public Entrant(String name, String status, String userId, Double latitude, Double longitude) {
        this.name = name;
        this.status = status;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getUserId() {
        return userId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}