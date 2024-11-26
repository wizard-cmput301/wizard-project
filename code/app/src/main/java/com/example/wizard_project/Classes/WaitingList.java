package com.example.wizard_project.Classes;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

public class WaitingList {

    private final FirebaseFirestore db;
    private final String eventId; // Associated event ID

    public WaitingList(String eventId) {
        this.eventId = eventId;
        this.db = FirebaseFirestore.getInstance();
    }

    // Check if a user is already on the waiting list
    public void isUserOnWaitingList(String userId, OnCheckCompleteListener listener) {
        // Validate inputs
        if (eventId == null || userId == null) {
            Log.e("WaitingList", "Error: eventId or userId is null.");
            listener.onFailure(new IllegalArgumentException("eventId and userId must not be null."));
            return;
        }

        // Query Firestore to check if the user is on the waiting list
        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("WaitingList", "User is on the waiting list.");
                        listener.onComplete(true);
                    } else {
                        Log.d("WaitingList", "User is not on the waiting list.");
                        listener.onComplete(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("WaitingList", "Error checking waiting list: " + e.getMessage(), e);
                    listener.onFailure(e);
                });
    }


    // Add a user to the waiting list
    public void addUserToWaitingList(User user, String userId, OnActionCompleteListener listener) {
        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    // Remove a user from the waiting list
    public void removeUserFromWaitingList(String userId, OnActionCompleteListener listener) {
        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    // Listener interface for checking user status
    public interface OnCheckCompleteListener {
        void onComplete(boolean isOnList);

        void onFailure(Exception e);
    }

    // Listener interface for adding/removing actions
    public interface OnActionCompleteListener {
        void onSuccess();

        void onFailure(Exception e);
    }
}
