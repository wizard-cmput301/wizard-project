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

/**
 * WaitingListController manages interactions with the waiting list for events in Firestore.
 */
public class WaitingListController {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Checks if a user is on the waiting list for the given event.
     *
     * @param eventId  The ID of the event.
     * @param userId   The ID of the user.
     * @param callback The callback to handle success or failure.
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
                    callback.onComplete(documentSnapshot.exists());
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
            callback.onFailure(new IllegalArgumentException("Event ID, User, and User ID must not be null."));
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", user.getName());
        userData.put("latitude", latitude);
        userData.put("longitude", longitude);
        userData.put("status", status);

        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Removes a user from the waiting list for the given event.
     *
     * @param eventId  The ID of the event.
     * @param userId   The ID of the user to remove.
     * @param callback The callback to handle success or failure.
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
     * @param eventId   The ID of the event.
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

    /**
     * Updates the status of a user in the waiting list for a specific event.
     *
     * @param eventId   The ID of the event for which the user's status is being updated. Must not be null.
     * @param userId    The ID of the user whose status needs to be updated. Must not be null.
     * @param newStatus The new status to set for the user (e.g., "Enrolled", "Cancelled"). Must not be null.
     * @param callback  A callback to handle the result of the update operation, providing onSuccess or onFailure methods.
     */
    public void updateUserStatus(String eventId, String userId, String newStatus, OnActionCompleteListener callback) {
        if (eventId == null || userId == null || newStatus == null) {
            callback.onFailure(new IllegalArgumentException("Event ID, User ID, and New Status must not be null."));
            return;
        }

        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }


    /**
     * Retrieves the status of a user in the waiting list for a specific event.
     *
     * @param eventId  The ID of the event.
     * @param userId   The ID of the user whose status is being retrieved.
     * @param callback The callback to handle the status or failure.
     */
    public void getUserStatus(String eventId, String userId, OnStatusFetchedCallback callback) {
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String status = documentSnapshot.exists() ? documentSnapshot.getString("status") : "Not Found";
                    callback.onStatusFetched(status != null ? status : "Unknown");
                })
                .addOnFailureListener(callback::onFailure);
    }




    // Callback interfaces
    public interface OnCheckCompleteListener {
        void onComplete(boolean isOnList);

        void onFailure(Exception e);
    }

    // Callback interface for adding/removing users to/from the waiting list
    public interface OnActionCompleteListener {
        void onSuccess();

        void onFailure(Exception e);
    }

    public interface OnStatusFetchedCallback {
        void onStatusFetched(String status);

        void onFailure(Exception e);
    }
}
