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
        // set up the user

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
        User newUser = new User(
                deviceId,           // deviceId
                "",                 // Email
                "",                 // location
                false,               // isAdmin
                false,              // isEntrant
                false,              // isOrganizer
                "",                 // name
                "",                 // phoneNumber
                ""                  // profile pic uri
        );
        storeUser(newUser);
    }

    /**
     * Retrieves the unique device ID from the user's device.
     *
     * @return A string representing the device ID.
     */
    public String retrieveDeviceId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Stores the device ID in the Firestone database if the user is new.
     * The user's admin status is set to false by default.
     *
     * @param newUser A class representing the data stored from this user
     */
    private void storeUser(User newUser) {
        // Create a user data map with the user data
        String deviceId = newUser.getDeviceId();

        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("deviceId",deviceId);
        deviceData.put("email",newUser.getEmail());
        deviceData.put("location",newUser.getLocation());
        deviceData.put("IsAdmin", newUser.isAdmin());
        deviceData.put("IsEntrant", newUser.isEntrant());
        deviceData.put("isOrganizer", newUser.isOrganizer());
        deviceData.put("name", newUser.getName());
        deviceData.put("phoneNumber", newUser.getPhoneNumber());
        deviceData.put("photoId", newUser.getProfilePictureUri());

        // Check if the device already exists in the database
        db.collection("users").document(deviceId).get().addOnCompleteListener( task -> {
            if (task.isSuccessful()) {

                DocumentSnapshot document = task.getResult();
                // If the user is new add them to the database
                // otherwise if not found in the database, add them

                if (document.exists()) {
                    newUser.setEmail((String) document.get("email"));
                    newUser.setLocation((String) document.get("location"));
                    newUser.setAdmin((Boolean) document.get("IsAdmin"));
                    newUser.setEntrant((Boolean) document.get("IsEntrant"));
                    newUser.setOrganizer((Boolean) document.get("isOrganizer"));
                    newUser.setName((String) document.get("name"));
                    newUser.setPhoneNumber((String) document.get("phoneNumber"));
                    newUser.setProfilePictureUri((String) document.get("photoId"));
                }
                else {
                    db.collection("users").document(deviceId).set(deviceData);
                }
            }
        });
    }
}