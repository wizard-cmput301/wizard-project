package com.example.wizard_project;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
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

    private ActivityMainBinding binding;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up view binding and content view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the toolbar
        setSupportActionBar(binding.toolbar);

        // Initialize navigation components
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Connect NavController to BottomNavigationView
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Home Button
            if (item.getItemId() == R.id.nav_home) {
                navController.navigate(R.id.HomeFragment);
                return true;
            }
            // TODO: Camera Button
            // TODO: Menu Button
            return false;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Retrieve and store the device ID
        String deviceId = retrieveDeviceId();
        storeDeviceId(deviceId);
    }

    /**
     * Retrieves the unique device ID from the user's device.
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