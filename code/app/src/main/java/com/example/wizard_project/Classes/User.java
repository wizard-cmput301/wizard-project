package com.example.wizard_project.Classes;

import com.example.wizard_project.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The User class represents a user in the application, and provides methods to manage user data.
 * This class is designed to interact with Firestore to store and retrieve user data.
 */
public class User {
    private final FirebaseFirestore db;
    private String deviceId;
    private String name;
    private String email;
    private String phoneNumber;
    private boolean isAdmin;
    private boolean isEntrant;
    private boolean isOrganizer;
    private String profilePictureUri;
    private String profilePath;
    private DocumentReference userRef;

    /**
     * Default constructor initializes fields with default values and sets up Firestore reference.
     */
    public User() {
        // Initialize fields with default values if needed
        db = FirebaseFirestore.getInstance();
        this.deviceId = "";
        this.email = "";
        this.isAdmin = false;
        this.isEntrant = false;
        this.isOrganizer = false;
        this.name = "";
        this.phoneNumber = "";
        this.profilePictureUri = "";
        this.profilePath = "";
        initializeDocumentReference();
    }

    /**
     * Constructor with all fields to initialize a User object.
     *
     * @param deviceId          The user's device ID.
     * @param email             The user's email.
     * @param isAdmin           Whether the user is an admin.
     * @param isEntrant         Whether the user is an entrant.
     * @param isOrganizer       Whether the user is an organizer.
     * @param name              The user's name.
     * @param phoneNumber       The user's phone number.
     * @param profilePictureUri The URI for the user's profile picture.
     * @param profilePath       The URI for the user's profile picture.
     */
    public User(String deviceId, String email,boolean isAdmin, boolean isEntrant, boolean isOrganizer, String name, String phoneNumber, String profilePictureUri, String profilePath) {
        db = FirebaseFirestore.getInstance();
        this.deviceId = deviceId;
        this.email = email;
        this.isAdmin = isAdmin;
        this.isEntrant = isEntrant;
        this.isOrganizer = isOrganizer;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profilePictureUri = profilePictureUri;
        this.profilePath = profilePath;
        initializeDocumentReference();
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
     */
    public void deleteUser() {
        this.deviceId = "";
        this.email = "";
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
        this.isAdmin = (Boolean) document.get("IsAdmin");
        this.isEntrant = (Boolean) document.get("IsEntrant");
        this.isOrganizer = (Boolean) document.get("isOrganizer");
        this.name = (String) document.get("name");
        this.phoneNumber = (String) document.get("phoneNumber");
        this.profilePictureUri = (String) document.get("photoId");
        this.profilePath = (String) document.get("profilePath");
        initializeDocumentReference();
    }

    /**
     * Generates a deterministic profile picture based on the user's name.
     *
     * @return The resource ID of the drawable representing the profile picture.
     */
    public int profilePictureGenerator() {
        int sum = 0;

        // Calculate the sum of ASCII values of the name
        for (int i = 0; i < (this.name).length(); i++) {
            sum += (this.name).charAt(i);
        }
        // Calculate the modulo of the sum
        int index = sum % 10;

        // Array of drawable resources
        int[] drawables = {
                R.drawable.black,               // 0
                R.drawable.blue,                // 1
                R.drawable.green,               // 2
                R.drawable.yellow,              // 3
                R.drawable.orange,              // 4
                R.drawable.pink,                // 5
                R.drawable.darkred,             // 6
                R.drawable.brown,               // 7
                R.drawable.grey,                // 8
                R.drawable.event_wizard_logo    // 9
        };

        // Return the profile picture based on the calculated index
        return drawables[index];
    }
}
