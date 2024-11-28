package com.example.wizard_project.Classes;

/**
 * Represents an entrant in an event.
 */
public class Entrant {
    private String name;
    private String status;
    private String userId;
    private Double latitude;
    private Double longitude;

    /**
     * Constructs a new Entrant object with the provided details.
     *
     * @param name      The name of the entrant.
     * @param status    The status of the entrant (e.g., "Waitlisted", "Selected").
     * @param userId    The unique identifier for the entrant.
     * @param latitude  The latitude of the entrant's location (nullable).
     * @param longitude The longitude of the entrant's location (nullable).
     */
    public Entrant(String name, String status, String userId, Double latitude, Double longitude) {
        this.name = name;
        this.status = status;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
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