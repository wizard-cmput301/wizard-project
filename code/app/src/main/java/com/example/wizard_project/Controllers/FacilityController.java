package com.example.wizard_project.Controllers;

import android.util.Log;
import android.widget.Toast;

import com.example.wizard_project.Classes.Facility;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class FacilityController {
    private FirebaseFirestore db;

    public FacilityController() {
        db = FirebaseFirestore.getInstance();
    }

    public Facility createFacility(String userId, String facility_name, String facility_location) {
        Facility newFacility = new Facility(userId, facility_name, facility_location);

        Map<String, Object> facilityData = new HashMap<>();
        facilityData.put("userId", newFacility.getUserId());
        facilityData.put("name", newFacility.getFacility_name());
        facilityData.put("location", newFacility.getFacility_location());
        facilityData.put("facilityId", newFacility.getFacilityId());

        db.collection("facilities").document(newFacility.getFacilityId()).set(facilityData)
                .addOnSuccessListener(aVoid -> Log.d("FacilityCreated", "Successfully added facility."))
                .addOnFailureListener(e -> { Log.e("FacilityError", "Failed to create facility.", e); });
        return newFacility;
    }

    public Facility getFacility(String userId) {
        Facility newFacility = new Facility(userId, "", "");
        db.collection("facilities").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(documentSnapshots -> {
                    if (!documentSnapshots.isEmpty()) {
                        newFacility.setUserData(documentSnapshots.getDocuments().get(0));
                    }
                });
        return newFacility;
    }

    public void updateFacility(Facility facility) {
        DocumentReference facilityRef = db.collection("facilities").document(facility.getFacilityId());

        facilityRef.update("name", facility.getFacility_name());
        facilityRef.update("location", facility.getFacility_location());
    }
}
