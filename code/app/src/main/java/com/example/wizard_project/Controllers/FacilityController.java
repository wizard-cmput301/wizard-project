package com.example.wizard_project.Controllers;

import android.util.Log;

import com.example.wizard_project.Classes.Facility;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * FacilityController acts as a communicator between the database and Facility objects.
 * Facilitates the addition, retrieval, and updating of Facility objects.
 */
public class FacilityController {
    private FirebaseFirestore db;

    /**
     * Constructs a FacilityController with a database instance.
     */
    public FacilityController() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new facility with its attributes and adds it to the database.
     * @param userId The device ID of the user.
     * @param facility_name The name of the facility.
     * @param facility_location The location of the facility.
     * @return The newly created Facility object.
     * adds a facility to the database.
     * @param newFacility a placeholder facility Object
     */
    public void createFacility(Facility newFacility) {
        // Create a new facility object with the inputted arguments.

        // Create a new hashmap containing each facility attribute and its value.
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
                .addOnFailureListener(e -> { Log.e("FacilityError", "Failed to create facility.", e); });

    }

    /**
     * Retrieves a facility from the database.
     *
     * @param userId   The device ID of the user.
     * @param callback A callback interface containing the retrieved facility.
     */
    public void getFacility(String userId, facilityCallback callback) {
        // Create a new facility object.
        Facility newFacility = new Facility(userId,"","",  "", "", "" );

        // Populate the facility object with the facility info from the database.
        db.collection("facilities").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(documentSnapshots -> {
                    if (!documentSnapshots.isEmpty()) {
                        DocumentSnapshot facilityRef = documentSnapshots.getDocuments().get(0);
                        newFacility.setFacilityData(facilityRef);
                        callback.onCallback(newFacility);
                    }
                    else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RetrievalError", "Error retrieving facility data:", e);
                    callback.onCallback(null);
                });
    }

    /**
     * Updates the values of a facility in the database.
     * @param facility The facility with the updated values.
     */
    public void updateFacility(Facility facility) {
        // Retrieve the facility document.
        DocumentReference facilityRef = db.collection("facilities").document(facility.getFacilityId());

        // Update the facility fields.
        facilityRef.update("name", facility.getFacility_name());
        facilityRef.update("location", facility.getFacility_location());

        // Update all related events.
        db.collection("events").whereEqualTo("facilityId", facility.getFacilityId()).get()
                .addOnSuccessListener(documentSnapshots -> {
                    if (!documentSnapshots.isEmpty()) {
                        for (int i = 0; i < documentSnapshots.size(); i++) {
                            DocumentSnapshot doc = documentSnapshots.getDocuments().get(i);
                            DocumentReference eventRef = doc.getReference();
                            eventRef.update("location", facility.getFacility_name());
                        }
                    }
                })
                .addOnFailureListener(e -> { Log.e("UpdateError", "Failed to retrieve events for a facility.", e);
                });
    }
    /**
     * Updates the values of a facility in the database.
     * @param facility The facility to update
     * @param feild The feild to update
     * @param update the update
     */
    public void updateFeild(Facility facility, String feild,String update) {
        // Retrieve the facility document.
        DocumentReference facilityRef = db.collection("facilities").document(facility.getFacilityId());

        // Update the facility fields.
        facilityRef.update(feild,update);

    }

    /**
     * An interface that facilitates the retrieval of a facility from the database.
     */
    public interface facilityCallback {
        void onCallback(Facility facility);
    }
}
