package com.example.wizard_project.Controllers;

import android.util.Log;

import com.example.wizard_project.Classes.User;
import com.google.firebase.firestore.FirebaseFirestore;

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
     * @param callback  The callback to handle success or failure.
     */
    public void addUserToWaitingList(String eventId, User user, String userId, OnActionCompleteListener callback) {
        if (eventId == null || user == null || userId == null) {
            callback.onFailure(new IllegalArgumentException("Event ID, User, and User ID must not be null."));
            return;
        }

        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
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
}
