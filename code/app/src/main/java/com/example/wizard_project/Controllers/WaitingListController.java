package com.example.wizard_project.Controllers;

import android.util.Log;

import com.example.wizard_project.Classes.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaitingListController {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Callback interface for checking user status on the waiting list
    public interface OnCheckCompleteListener {
        void onComplete(boolean isOnList);
        void onFailure(Exception e);
    }

    // Callback interface for adding/removing users to/from the waiting list
    public interface OnActionCompleteListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Checks if a user is on the waiting list for the given event.
     *
     * @param eventId   The ID of the event.
     * @param userId    The ID of the user.
     * @param callback  The callback to handle success or failure.
     */
    public void isUserOnWaitingList(String eventId, String userId, OnCheckCompleteListener callback) {
        if (eventId == null || userId == null) {
            callback.onFailure(new IllegalArgumentException("Event ID and User ID must not be null."));
            return;
        }

        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onComplete(true);
                    } else {
                        callback.onComplete(false);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds a user to the waiting list for the given event.
     *
     * @param eventId   The ID of the event.
     * @param user      The user object to add.
     * @param userId    The ID of the user.
     * @param latitude  The latitude of the user's location.
     * @param longitude The longitude of the user's location.
     * @param status    The user's event status (e.g., "Waitlisted", "Selected", etc).
     * @param callback  The callback to handle success or failure.
     */
    public void addUserToWaitingList(String eventId, User user, String userId, Double latitude, Double longitude, String status, OnActionCompleteListener callback) {
        if (eventId == null || user == null || userId == null) {
            Log.e("FirestoreError", "Invalid arguments: eventId, user, or userId is null.");
            callback.onFailure(new IllegalArgumentException("Event ID, User, and User ID must not be null."));
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", user.getName());
        userData.put("latitude", latitude);
        userData.put("longitude", longitude);
        userData.put("status", status);

        Log.d("WaitingListController", "Attempting to add user to waiting list: " + userData);

        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreSuccess", "User added to waiting list successfully.");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to add user to waiting list.", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Removes a user from the waiting list for the given event.
     *
     * @param eventId   The ID of the event.
     * @param userId    The ID of the user to remove.
     * @param callback  The callback to handle success or failure.
     */
    public void removeUserFromWaitingList(String eventId, String userId, OnActionCompleteListener callback) {
        if (eventId == null || userId == null) {
            callback.onFailure(new IllegalArgumentException("Event ID and User ID must not be null."));
            return;
        }

        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves the locations of all entrants in the waiting list for a given event.
     *
     * @param eventId The ID of the event.
     * @param onSuccess Callback invoked with a list of locations on success.
     * @param onFailure Callback invoked on failure.
     */
    public void getEntrantLocations(String eventId, OnSuccessListener<List<double[]>> onSuccess, OnFailureListener onFailure) {
        if (eventId == null) {
            onFailure.onFailure(new IllegalArgumentException("Event ID must not be null."));
            return;
        }

        db.collection("events").document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<double[]> locations = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");
                        if (latitude != null && longitude != null) {
                            locations.add(new double[]{latitude, longitude});
                        }
                    }
                    onSuccess.onSuccess(locations);
                })
                .addOnFailureListener(onFailure::onFailure);
    }

    public void getUserStatus(String eventId, String userId, OnStatusFetchedCallback callback) {
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        callback.onStatusFetched(status != null ? status : "Unknown");
                    } else {
                        callback.onStatusFetched("Not Found");
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface OnStatusFetchedCallback {
        void onStatusFetched(String status);
        void onFailure(Exception e);
    }
}
