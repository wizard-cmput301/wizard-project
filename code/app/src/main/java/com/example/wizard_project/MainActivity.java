package com.example.wizard_project;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import com.google.android.material.snackbar.Snackbar;

import com.example.wizard_project.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private Button enterEventButton;
    private Button manageFacilityButton;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get the device ID from the user
        String deviceId = retrieveDeviceId();

        // Store the device ID in the database if the user is new
        storeDeviceId(deviceId);

        // Initialize buttons
        enterEventButton = findViewById(R.id.enter_event_button);
        manageFacilityButton = findViewById(R.id.manage_facility_button);

        // Enter Event Button
        enterEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Create/Manage Facility Button
        manageFacilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    /**
     * Gets the unique device ID from the user's device.
     *
     * @return A string representing the device ID.
     */
    private String retrieveDeviceId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Stores the device ID in the Firestone database if the user is new.
     * The user's admin status is set to false by default.
     *
     * @param deviceId A string representing the unique device ID to be stored in the database.
     */
    private void storeDeviceId(String deviceId) {
        // Create a user data map with the device ID and admin status
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("deviceId", deviceId);
        deviceData.put("admin", false); // Set admin status to false by default

        // Check if the device already exists in the database
        db.collection("users").document(deviceId).get().addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                // If the device ID is not found in the database, add it
                if (!document.exists()) {
                    db.collection("users").document(deviceId).set(deviceData);
                }
            }
        });
    }
}