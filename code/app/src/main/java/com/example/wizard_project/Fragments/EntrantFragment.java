package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.wizard_project.R;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.databinding.FragmentEntrantBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * EntrantFragment represents the UI and functionality for entrants.
 */
public class EntrantFragment extends Fragment {

    private FragmentEntrantBinding binding;
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;
    private String eventDescription;

    private String userId = "123"; // Replace with actual user ID (e.g., deviceId)
    private User currentUser; // To hold the User object after fetching data

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntrantBinding.inflate(inflater, container, false);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve data from bundle
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            eventName = getArguments().getString("eventName");
            eventDescription = getArguments().getString("eventDescription");
        }

        // Set event details in UI
        binding.eventName.setText(eventName);
        binding.eventDescription.setText(eventDescription);

        // Fetch current user data from Firestore
        fetchCurrentUser();

        // Set up button listeners
        binding.joinButton.setOnClickListener(v -> joinWaitingList());
        binding.leaveButton.setOnClickListener(v -> leaveWaitingList());

        return binding.getRoot();
    }

    private void fetchCurrentUser() {
        // Fetch user data from Firestore
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Populate currentUser with the data from Firestore
                        currentUser = documentSnapshot.toObject(User.class); // Assuming Firestore can deserialize to User
                    } else {
                        Toast.makeText(getContext(), "User not found in Firestore.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error fetching user data.", Toast.LENGTH_SHORT).show());
    }

    private void joinWaitingList() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "User data not available. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 1: Check if user is registered as an entrant in Firestore
        if (!currentUser.isEntrant()) {
            Toast.makeText(getContext(), "You must be an entrant to join the waiting list.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 2: Check if the user is already in the waiting list for the event
        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .get()
                .addOnSuccessListener(waitingListSnapshot -> {
                    if (waitingListSnapshot.exists()) {
                        Toast.makeText(getContext(), "You are already on the waiting list.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Step 3: Add user to waiting list
                        addUserToWaitingList();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error checking waiting list.", Toast.LENGTH_SHORT).show());
    }

    private void addUserToWaitingList() {
        // Add currentUser to the event's waiting list
        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .set(currentUser) // Add the full user object if Firestore supports User serialization
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Joined waiting list successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to join waiting list.", Toast.LENGTH_SHORT).show());
    }

    private void leaveWaitingList() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "User data not available. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId)
                .collection("waitingList").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Left the waiting list successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to leave the waiting list.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
