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
        // Initialize fields with default values if needed
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
     * @param profilePath The URI for the user's profile picture.

     */
    public User(String deviceId, String email, String location, boolean isAdmin, boolean isEntrant, boolean isOrganizer, String name, String phoneNumber, String profilePictureUri,String profilePath) {
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

    // Getters and Setters with corresponding Firestore updates
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        initializeDocumentReference();
        updateFieldInDatabase("deviceId", deviceId);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        updateFieldInDatabase("email", email);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        updateFieldInDatabase("location", location);
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
        updateFieldInDatabase("isAdmin", admin);
    }

    public boolean isEntrant() {
        return isEntrant;
    }

    public void setEntrant(boolean entrant) {
        isEntrant = entrant;
        updateFieldInDatabase("isEntrant", entrant);
    }

    public boolean isOrganizer() {
        return isOrganizer;
    }

    public void setOrganizer(boolean organizer) {
        isOrganizer = organizer;
        updateFieldInDatabase("isOrganizer", organizer);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateFieldInDatabase("name", name);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        updateFieldInDatabase("phoneNumber", phoneNumber);
    }

    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    public void setProfilePictureUri(String profilePictureUri) {
        this.profilePictureUri = profilePictureUri;
        updateFieldInDatabase("photoId", profilePictureUri);
    }
    public String getProfilePath() {
        return this.profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
        updateFieldInDatabase("profilePath", profilePath);
    }

    /**
     * Wipes the user's data from memory.
     * TODO: rename
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
     * @return A string representing the user's role.
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
     * @param field The field to be updated.
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
