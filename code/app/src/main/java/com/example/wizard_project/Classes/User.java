package com.example.wizard_project.Classes;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The User class represents a user in the application, and provides methods to manage user data.
 * This class is designed to interact with Firestore to store and retrieve user data.
 */
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
    private String profilePath;

    private FirebaseFirestore db;
    private DocumentReference userRef;

    /**
     * Default constructor initializes fields with default values and sets up Firestore reference.
     */
    public User() {
        db = FirebaseFirestore.getInstance();
        this.deviceId = "";
        initializeDocumentReference();
        this.email = "";
        this.location = "";
        this.isAdmin = false;
        this.isEntrant = false;
        this.isOrganizer = false;
        this.name = "";
        this.phoneNumber = "";
        this.profilePictureUri = "";
        this.profilePath = "";
    }

    /**
     * Constructor with all fields to initialize a User object.
     *
     * @param deviceId          The user's device ID.
     * @param email             The user's email.
     * @param location          The user's location.
     * @param isAdmin           Whether the user is an admin.
     * @param isEntrant         Whether the user is an entrant.
     * @param isOrganizer       Whether the user is an organizer.
     * @param name              The user's name.
     * @param phoneNumber       The user's phone number.
     * @param profilePictureUri The URI for the user's profile picture.
     * @param profilePath       The path for the user's profile picture in storage.
     */
    public User(String deviceId, String email, String location, boolean isAdmin, boolean isEntrant,
                boolean isOrganizer, String name, String phoneNumber, String profilePictureUri, String profilePath) {
        db = FirebaseFirestore.getInstance();
        this.deviceId = deviceId;
        initializeDocumentReference();
        this.email = email;
        this.location = location;
        this.isAdmin = isAdmin;
        this.isEntrant = isEntrant;
        this.isOrganizer = isOrganizer;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profilePictureUri = profilePictureUri;
        this.profilePath = profilePath;
    }

    /**
     * Gets the user's device ID.
     * @return The user's device ID.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device ID and updates the database.
     * @param deviceId The user's device ID.
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        initializeDocumentReference();
        updateFieldInDatabase("deviceId", deviceId);
    }

    /**
     * Gets the user's email.
     * @return The user's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email and updates the database.
     * @param email The user's email.
     */
    public void setEmail(String email) {
        this.email = email;
        updateFieldInDatabase("email", email);
    }

    /**
     * Gets the user's location.
     * @return The user's location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the user's location and updates the database.
     * @param location The user's location.
     */
    public void setLocation(String location) {
        this.location = location;
        updateFieldInDatabase("location", location);
    }

    /**
     * Checks if the user is an admin.
     * @return True if the user is an admin, false otherwise.
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Sets the user's admin status and updates the database.
     * @param admin True if the user is an admin, false otherwise.
     */
    public void setAdmin(boolean admin) {
        isAdmin = admin;
        updateFieldInDatabase("isAdmin", admin);
    }

    /**
     * Checks if the user is an entrant.
     * @return True if the user is an entrant, false otherwise.
     */
    public boolean isEntrant() {
        return isEntrant;
    }

    /**
     * Sets the user's entrant status and updates the database.
     * @param entrant True if the user is an entrant, false otherwise.
     */
    public void setEntrant(boolean entrant) {
        isEntrant = entrant;
        updateFieldInDatabase("isEntrant", entrant);
    }

    /**
     * Checks if the user is an organizer.
     * @return True if the user is an organizer, false otherwise.
     */
    public boolean isOrganizer() {
        return isOrganizer;
    }

    /**
     * Sets the user's organizer status and updates the database.
     * @param organizer True if the user is an organizer, false otherwise.
     */
    public void setOrganizer(boolean organizer) {
        isOrganizer = organizer;
        updateFieldInDatabase("isOrganizer", organizer);
    }

    /**
     * Gets the user's name.
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name and updates the database.
     * @param name The user's name.
     */
    public void setName(String name) {
        this.name = name;
        updateFieldInDatabase("name", name);
    }

    /**
     * Gets the user's phone number.
     * @return The user's phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the user's phone number and updates the database.
     * @param phoneNumber The user's phone number.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        updateFieldInDatabase("phoneNumber", phoneNumber);
    }

    /**
     * Gets the URI for the user's profile picture.
     * @return The URI for the user's profile picture.
     */
    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    /**
     * Sets the URI for the user's profile picture and updates the database.
     * @param profilePictureUri The URI for the user's profile picture.
     */
    public void setProfilePictureUri(String profilePictureUri) {
        this.profilePictureUri = profilePictureUri;
        updateFieldInDatabase("photoId", profilePictureUri);
    }

    /**
     * Gets the path for the user's profile picture in storage.
     * @return The path for the user's profile picture in storage.
     */
    public String getProfilePath() {
        return this.profilePath;
    }

    /**
     * Sets the path for the user's profile picture in storage and updates the database.
     * @param profilePath The path for the user's profile picture in storage.
     */
    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
        updateFieldInDatabase("profilePath", profilePath);
    }

    /**
     * Deletes the user data by resetting all fields to default values.
     */
    public void DeleteUser() {
        this.deviceId = "";
        this.email = "";
        this.location = "";
        this.isAdmin = false;
        this.isEntrant = false;
        this.isOrganizer = false;
        this.name = "";
        this.phoneNumber = "";
        this.profilePictureUri = "";
        this.profilePath = "";
    }

    /**
     * Generates a string representation of the user's role.
     *
     * @return A string representing the user's role, e.g., "Admin", "Organizer", "Entrant".
     */
    public String getUserToString() {
        if (this.isAdmin) {
            return "Admin";
        } else if (this.isOrganizer) {
            return "Organizer";
        } else if (this.isEntrant) {
            return "Entrant";
        }
        return "User";
    }

    /**
     * Initializes the user document reference if the device ID is valid.
     */
    private void initializeDocumentReference() {
        if (deviceId != null && !deviceId.isEmpty()) {
            userRef = db.collection("users").document(deviceId);
        } else {
            userRef = null;
        }
    }

    /**
     * Updates a specific field in the Firestore user document.
     *
     * @param field The field to be updated in Firestore.
     * @param value The new value for the field.
     */
    private void updateFieldInDatabase(String field, Object value) {
        if (userRef != null) {
            userRef.update(field, value)
                    .addOnSuccessListener(aVoid -> System.out.println("Field " + field + " updated successfully."))
                    .addOnFailureListener(e -> System.err.println("Failed to update field " + field + ": " + e.getMessage()));
        } else {
            System.err.println("User Document Reference is null.");
        }
    }

    /**
     * Populates the user object with data from a Firestore document.
     *
     * @param document The Firestore document containing user data.
     */
    public void setUserData(DocumentSnapshot document) {
        this.deviceId = (String) document.get("deviceId");
        this.email = (String) document.get("email");
        this.location = (String) document.get("location");
        this.isAdmin = (Boolean) document.get("IsAdmin");
        this.isEntrant = (Boolean) document.get("IsEntrant");
        this.isOrganizer = (Boolean) document.get("isOrganizer");
        this.name = (String) document.get("name");
        this.phoneNumber = (String) document.get("phoneNumber");
        this.profilePictureUri = (String) document.get("photoId");
        this.profilePath = (String) document.get("profilePath");
        initializeDocumentReference();
    }
}
