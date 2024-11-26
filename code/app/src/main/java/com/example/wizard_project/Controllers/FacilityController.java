package com.example.wizard_project.Controllers;

import android.util.Log;

import com.example.wizard_project.Classes.Facility;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * FacilityController acts as a communicator between the database and Facility objects.
 * It provides methods to create, retrieve, update, and delete facilities in the Firestore database.
 */
public class FacilityController {
    private final FirebaseFirestore db;

    /**
     * Constructs a FacilityController with a database instance.
     */
    public FacilityController() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new facility with its attributes, adds it to the database, and updates the user's isOrganizer field.
     *
     * @param newFacility The facility object to add.
     * @param userId      The ID of the user creating the facility.
     * @param callback    Callback for success or failure.
     */
    public void createFacility(Facility newFacility, String userId, createCallback callback) {
        Map<String, Object> facilityData = new HashMap<>();
        facilityData.put("userId", newFacility.getUserId());
        facilityData.put("name", newFacility.getFacility_name());
        facilityData.put("location", newFacility.getFacility_location());
        facilityData.put("facilityId", newFacility.getFacilityId());
        facilityData.put("facility_imagePath", newFacility.getFacilityImagePath());
        facilityData.put("posterUri", newFacility.getposterUri());

        // Create the facility document in the database.
        db.collection("facilities").document(newFacility.getFacilityId()).set(facilityData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FacilityController", "Successfully added facility.");
                    // Update the user's isOrganizer field
                    updateIsOrganizer(userId, true, new updateCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("FacilityController", "User isOrganizer updated to true.");
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("FacilityController", "Facility created but failed to update user role.", e);
                            callback.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("FacilityController", "Failed to create facility.", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Retrieves a facility associated with a specific user from Firestore.
     *
     * @param userId   The device ID of the user.
     * @param callback A callback interface containing the retrieved facility.
     */
    public void getFacility(String userId, facilityCallback callback) {
        db.collection("facilities").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        Facility facility = new Facility();
                        facility.setFacilityData(documentSnapshot);
                        callback.onCallback(facility);
                    } else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FacilityController", "Error fetching facility", e);
                    callback.onCallback(null);
                });
    }

    /**
     * Updates a facility's details in the database.
     *
     * @param facility The facility object with updated values.
     */
    public void updateFacility(Facility facility, updateCallback callback) {
        if (facility.getFacilityId() == null || facility.getFacilityId().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Facility ID is null or empty"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", facility.getFacility_name());
        updates.put("location", facility.getFacility_location());

        db.collection("facilities").document(facility.getFacilityId()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FacilityController", "Facility updated successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FacilityController", "Error updating facility", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Updates a specific field of a facility in the database.
     *
     * @param facility The facility to update
     * @param field    The field to update
     * @param update   The new value for the field.
     */
    public void updateField(Facility facility, String field, String update) {
        db.collection("facilities").document(facility.getFacilityId()).update(field, update)
                .addOnFailureListener(e -> Log.e("FacilityController", "Error updating field.", e));
    }

    /**
     * Deletes a facility from the database.
     *
     * @param facilityId The ID of the facility to delete.
     * @param callback   Callback for success or failure.
     */
    public void deleteFacility(String facilityId, deleteCallback callback) {
        db.collection("facilities").document(facilityId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FacilityController", "Facility deleted successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FacilityController", "Error deleting facility", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Deletes a facility and all associated events from the database.
     *
     * @param facilityId The ID of the facility to delete.
     * @param callback   Callback for success or failure.
     */
    public void deleteFacilityWithEvents(String facilityId, deleteCallback callback) {
        // First, delete all events associated with the facility
        db.collection("events").whereEqualTo("facilityId", facilityId).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Batch delete all events
                        for (DocumentSnapshot eventDoc : querySnapshot.getDocuments()) {
                            db.collection("events").document(eventDoc.getId()).delete()
                                    .addOnSuccessListener(aVoid -> Log.d("FacilityController", "Deleted event: " + eventDoc.getId()))
                                    .addOnFailureListener(e -> Log.e("FacilityController", "Failed to delete event: " + eventDoc.getId(), e));
                        }
                    }
                    // After deleting events, delete the facility
                    deleteFacility(facilityId, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e("FacilityController", "Error fetching events for facility", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Updates the "isOrganizer" field for a user in Firestore.
     *
     * @param userId      The ID of the user to update.
     * @param isOrganizer The new value for the isOrganizer field.
     * @param callback    Callback for success or failure.
     */
    public void updateIsOrganizer(String userId, boolean isOrganizer, updateCallback callback) {
        db.collection("users").document(userId)
                .update("isOrganizer", isOrganizer)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FacilityController", "User isOrganizer updated to " + isOrganizer);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FacilityController", "Error updating isOrganizer", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Retrieves a list of all facilities from the database.
     *
     * @param callback A callback containing the retrieved facilities.
     */
    public void getFacilities(facilitiesCallback callback) {
        db.collection("facilities").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Facility> facilities = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Facility facility = new Facility();
                            facility.setFacilityData(document);
                            facilities.add(facility);
                        }
                        callback.onCallback(facilities);
                    } else {
                        Log.e("FacilityController", "Error retrieving facilities:", task.getException());
                        callback.onCallback(new ArrayList<>()); // Return empty list on failure
                    }
                });
    }

    // Callback Interfaces
    public interface facilityCallback {
        void onCallback(Facility facility);
    }

    public interface facilitiesCallback {
        void onCallback(ArrayList<Facility> facilities);
    }

    public interface deleteCallback {
        void onSuccess();

        void onFailure(Exception e);
    }

    public interface updateCallback {
        void onSuccess();

        void onFailure(Exception e);
    }

    public interface createCallback {
        void onSuccess();

        void onFailure(Exception e);
    }
}
