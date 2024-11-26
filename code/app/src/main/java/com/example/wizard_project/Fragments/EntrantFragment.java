package com.example.wizard_project.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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
import com.example.wizard_project.Classes.WaitingList;

import java.util.UUID;

/**
 * EntrantFragment represents the UI and functionality for entrants.
 */
public class EntrantFragment extends Fragment {

    private FragmentEntrantBinding binding;
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;
    private String eventDescription;

    private User currentUser;
    private String userId;
    private WaitingList waitingList;

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

        waitingList = new WaitingList(eventId);

        // Set event details in UI
        binding.eventName.setText(eventName);
        binding.eventDescription.setText(eventDescription != null ? eventDescription : "No Event Description");

        // Fetch current user data from Firestore
        fetchCurrentUser();
        // Set up navigation to ProfileFragment when the profile picture button is clicked
        View profilePictureButton = requireActivity().findViewById(R.id.profilePictureButton);

        profilePictureButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.ProfileFragment) {
                navController.navigate(R.id.action_EntrantFragment_to_ProfileFragment); // (temporary work around, this prevents app crashing when clicking the button twice)
            }
        });
        // Set up button listeners
        binding.joinButton.setOnClickListener(v -> joinWaitingList());
        binding.leaveButton.setOnClickListener(v -> leaveWaitingList());

        return binding.getRoot();
    }

    private String getDeviceId() {
        try {
            String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            if (deviceId != null && !deviceId.isEmpty()) {
                return deviceId;
            }
        } catch (Exception e) {
            Log.e("EntrantFragment", "Failed to get device ID", e);
        }

        // Fallback if ANDROID_ID fails
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String deviceId = prefs.getString("DEVICE_ID", null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            prefs.edit().putString("DEVICE_ID", deviceId).apply();
        }
        return deviceId;
    }

    private void fetchCurrentUser() {
        // Get the device ID
        String deviceId = getDeviceId();
        Log.d("EntrantFragment", "Device ID: " + deviceId);

        // Query Firestore to find the user by device ID
        db.collection("users")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // User found
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            userId = deviceId;
                            Log.d("EntrantFragment", "User data fetched successfully: " + documentSnapshot.getData());

                        }
                    } else {
                        // User not found
                        Log.d("EntrantFragment", "No user found with this device ID.");
                        Toast.makeText(getContext(), "No user found with this device ID.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error fetching user data
                    Log.e("EntrantFragment", "Error fetching user data: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error fetching user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void joinWaitingList() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "User data not available. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }


        waitingList.isUserOnWaitingList(userId, new WaitingList.OnCheckCompleteListener() {
            @Override
            public void onComplete(boolean isOnList) {
                if (isOnList) {
                    Toast.makeText(getContext(), "You are already on the waiting list.", Toast.LENGTH_SHORT).show();
                } else {
                    waitingList.addUserToWaitingList(currentUser, userId, new WaitingList.OnActionCompleteListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Joined waiting list successfully!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to join waiting list.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error checking waiting list.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveWaitingList() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "User data not available. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        waitingList.isUserOnWaitingList(userId, new WaitingList.OnCheckCompleteListener() {
            @Override
            public void onComplete(boolean isOnList) {
                if (isOnList) {
                    waitingList.removeUserFromWaitingList(userId, new WaitingList.OnActionCompleteListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Left the waiting list successfully!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to leave the waiting list.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "You are not on the waiting list for this event.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error checking waiting list status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
