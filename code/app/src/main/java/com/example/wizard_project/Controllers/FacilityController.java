package com.example.wizard_project.Controllers;

import android.util.Log;

import com.example.wizard_project.Classes.Facility;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * FacilityController acts as a communicator between the database and facility objects.
 * Facilitates the addition, retrieval, and updating of facility objects.
 */
public class FacilityController {
    private final FirebaseFirestore db;

    /**
     * Constructs a FacilityController with the database instance.
     */
    public FacilityController() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Adds a facility to the database.
     *
     * @param newFacility The facility objet to add.
     */
    public void createFacility(Facility newFacility) {
        Map<String, Object> facilityData = new HashMap<>();
        facilityData.put("userId", newFacility.getUserId());
        facilityData.put("name", newFacility.getFacility_name());
        facilityData.put("location", newFacility.getFacility_location());
        facilityData.put("facilityId", newFacility.getFacilityId());
        facilityData.put("facility_imagePath", newFacility.getFacilitymagePath());
        facilityData.put("posterUri", newFacility.getposterUri());

        // Create the facility document in the database.
        db.collection("facilities").document(newFacility.getFacilityId()).set(facilityData)
                .addOnSuccessListener(aVoid -> Log.d("FacilityCreated", "Successfully added facility."))
                .addOnFailureListener(e -> {
                    Log.e("FacilityError", "Failed to create facility.", e);
                });
    }

    /**
     * Retrieves a facility from the database based on the user ID.
     *
     * @param userId   The device ID of the user.
     * @param callback A callback containing the retrieved facility.
     */
    public void getFacility(String userId, facilityCallback callback) {
        // Create a new facility object.
        Facility newFacility = new Facility(userId, "", "", "", "", "");

        // Populate the facility object with the facility info from the database.
        db.collection("facilities").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(documentSnapshots -> {
                    if (!documentSnapshots.isEmpty()) {
                        DocumentSnapshot facilityRef = documentSnapshots.getDocuments().get(0);
                        newFacility.setFacilityData(facilityRef);
                        callback.onCallback(newFacility);
                    } else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RetrievalError", "Error retrieving facility data:", e);
                    callback.onCallback(null);
                });
    }

    /**
     * Updates a facility's details in the database.
     *
     * @param facility The facility objet with updated values.
     */
    public void updateFacility(Facility facility) {
        // Retrieve the facility document.
        DocumentReference facilityRef = db.collection("facilities").document(facility.getFacilityId());

        // Update the facility fields.
        facilityRef.update("name", facility.getFacility_name());
        facilityRef.update("location", facility.getFacility_location());
    }

    /**
     * Updates a specific field of a facility in the database.
     *
     * @param facility The facility to update
     * @param field    The field to update
     * @param update   The new value for the field.
     */
    public void updateFeild(Facility facility, String field, String update) {
        // Retrieve the facility document.
        DocumentReference facilityRef = db.collection("facilities").document(facility.getFacilityId());

        // Update the facility fields.
        facilityRef.update(field, update);
    }

    /**
     * Deletes a facility from the database.
     *
     * @param facility The facility to delete.
     */
    public void deleteFacility(Facility facility) {
        db.collection("facilities").document(facility.getFacilityId()).delete()
                .addOnSuccessListener(aVoid -> Log.d("FacilityDeleted", "Successfully deleted facility."))
                .addOnFailureListener(e -> {
                    Log.e("FacilityError", "Failed to delete facility.", e);
                });
    }


    /**
     * Retrieves a list of all facilities from the database.
     *
     * @param callback A callback containing the retrieved facilities.
     */
    public void getFacilities(facilitiesCallback callback) {
        db.collection("facilities")
                .get()
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

    /**
     * Callback interface for retrieving a single facility.
     */
    public interface facilityCallback {
        void onCallback(Facility facility);
    }

    /**
     * Callback interface for retrieving a list of facilities.
     */
    public interface facilitiesCallback {
        void onCallback(ArrayList<Facility> facilities);
    }
}