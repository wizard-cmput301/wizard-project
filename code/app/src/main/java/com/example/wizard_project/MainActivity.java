package com.example.wizard_project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.Entrant;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.EventController;
import com.example.wizard_project.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * MainActivity is the central activity for the EventWizard app.
 * It manages user authentication, navigation, and data storage in Firestore.
 * Features include:
 * - Retrieving and storing user data.
 * - Configuring UI based on user roles.
 * - Navigation between fragments for different user roles.
 */
public class MainActivity extends AppCompatActivity {
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private User currentUser;
    private User deleteUser;
    private boolean isAdminMode = false;
    private EventController eventController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up view binding and content view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check for location permissions
        checkAndRequestLocationPermissions();

        //check for notification permission
        checkNotifPermission();

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize navigation components
        setupNavigation();

        // Retrieve the device ID, then initialize the user
        String deviceId = retrieveDeviceId();
        initializeUser(deviceId, () -> {
            if (currentUser != null) {
                setProfilePic();
            } else {
                Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Perform the entrant lottery draw for any eligible events.
        checkEventDraw();
    }

    public void setProfilePic() {
        ImageButton profilePictureButton = findViewById(R.id.profilePictureButton);
        String profilePictureUri = currentUser.getProfilePictureUri();
        if (profilePictureUri != null && !profilePictureUri.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(profilePictureUri))
                    .circleCrop()
                    .into(profilePictureButton);
        } else if (!currentUser.getName().isEmpty()) {
            int draw = currentUser.profilePictureGenerator();
            Glide.with(this).load(draw).circleCrop().into(profilePictureButton);
        } else {
            Glide.with(this).load(R.drawable.noname).circleCrop().into(profilePictureButton);
        }
    }

    /**
     * Configures bottom navigation and toolbar visibility for specific fragments.
     */
    private void setupNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Connect NavController to BottomNavigationView
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                if (isAdminMode) {
                    exitAdminMode(bottomNavigationView);
                }
                navController.navigate(R.id.HomeFragment);
                return true;
            } else if (itemId == R.id.nav_camera) {
                // TODO: Add camera functionality
                return true;
            } else if (itemId == R.id.nav_menu) {
                // TODO: Add menu functionality
                return true;
            } else if (itemId == R.id.nav_profile_browse) {
                navController.navigate(R.id.AdminFragment);
                return true;
            } else if (itemId == R.id.nav_events_browse) {
                navController.navigate(R.id.AdminFragmentEventView);
                return true;
            } else if (itemId == R.id.nav_facility_browse) {
                navController.navigate(R.id.AdminFragmentFacilityView);
                return true;
            } else if (itemId == R.id.nav_image_browse) {
                navController.navigate(R.id.AdminFragmentImageView);
                return true;
            }
            return false;
        });

        // Listen for destination changes and adjust navigation as needed
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (isAdminFragment(destination.getId()) && !isAdminMode) {
                enterAdminMode(bottomNavigationView);
            } else if (destination.getId() == R.id.HomeFragment && isAdminMode) {
                exitAdminMode(bottomNavigationView);
            }
        });
    }

    /**
     * Configures the bottom navigation bar for general use (default).
     */
    private void setupGeneralNavigation(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
        isAdminMode = false;
    }

    /**
     * Configures the bottom navigation bar for admin use.
     */
    private void enterAdminMode(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_admin); // Admin menu resource
        isAdminMode = true;
    }

    /**
     * Resets the bottom navigation bar to general use when leaving admin mode.
     */
    private void exitAdminMode(BottomNavigationView bottomNavigationView) {
        setupGeneralNavigation(bottomNavigationView);
    }

    /**
     * Checks if the current destination is part of the admin section.
     */
    private boolean isAdminFragment(int destinationId) {
        return destinationId == R.id.AdminFragment ||
                destinationId == R.id.AdminFragmentEventView ||
                destinationId == R.id.AdminFragmentFacilityView ||
                destinationId == R.id.AdminFragmentImageView;
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
     * Initializes the user by fetching or creating the user document in Firestore.
     *
     * @param deviceId          The unique device ID used as the document ID in Firestore.
     * @param onUserInitialized A callback to indicate when the user is initialized.
     */
    private void initializeUser(String deviceId, Runnable onUserInitialized) {
        db.collection("users").document(deviceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                // If the document exists, load data into currentUser
                if (document.exists()) {
                    currentUser = new User();
                    currentUser.setUserData(document);
                    // If the document does not exist, create a new user
                } else {
                    currentUser = new User(deviceId, "", false, false, false, "", "", "", "");
                    Map<String, Object> userData = createUserDataMap(currentUser);
                    db.collection("users").document(deviceId).set(userData)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "New user created", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> {
                                currentUser = null;
                                Toast.makeText(this, "Failed to create user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
                // Error handling (unable to fetch document)
            } else {
                currentUser = null;
                Toast.makeText(this, "Error fetching user data: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
            onUserInitialized.run(); // Notify that user data is initialized (success or failure)
        });
    }

    /**
     * Creates a map of user data to be stored in Firestore.
     *
     * @param user The user object containing data to be stored.
     * @return A map of user data.
     */
    private Map<String, Object> createUserDataMap(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("deviceId", user.getDeviceId());
        userData.put("email", user.getEmail());
        userData.put("IsAdmin", user.isAdmin());
        userData.put("IsEntrant", user.isEntrant());
        userData.put("isOrganizer", user.isOrganizer());
        userData.put("name", user.getName());
        userData.put("phoneNumber", user.getPhoneNumber());
        userData.put("photoId", user.getProfilePictureUri());
        userData.put("profilePath", user.getProfilePath());
        return userData;
    }

    /**
     * Checks if location permissions are granted and requests them if not.
     */
    private void checkAndRequestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void checkNotifPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }
    /**
     * Returns the current user's data.
     *
     * @return The current User object or null if the user is not loaded.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Provides the current user to other components asynchronously.
     * If the user is already loaded into memory, it immediately executes the callback.
     * Otherwise, it fetches the user from the database and then invokes the callback.
     *
     * @param callback A callback that will be executed once the user is available.
     */
    public void getCurrentUserAsync(UserLoadCallback callback) {
        // Check if the user is already loaded into memory, if so execute the callback
        if (currentUser != null) {
            callback.onUserLoaded(currentUser);
        } else {
            // If the user is not loaded, fetch it from the database
            fetchCurrentUserFromDatabase(user -> {
                currentUser = user; // Cache the loaded user for future use
                callback.onUserLoaded(currentUser);
            });
        }
    }

    /**
     * Fetches the current user from the database asynchronously.
     * Once the user is retrieved, it invokes the provided callback.
     *
     * @param callback A callback to handle the loaded user object.
     */
    private void fetchCurrentUserFromDatabase(UserLoadCallback callback) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(retrieveDeviceId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    callback.onUserLoaded(user);
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Failed to load user", e));
    }

    /**
     * Checks eligibility for entrant draw for all events.
     * If conditions are met for an event, entrants are drawn.
     */
    public void checkEventDraw() {
        eventController = new EventController();
        LotterySystem lotterySystem = new LotterySystem();
        Date currentDate = new Date();
        eventController.fetchAllEvents(new EventController.eventCallback() {
            @Override
            public void onCallback(ArrayList<Event> events) {
                for (Event event: events) {
                    if (!event.isDrawn() && currentDate.after(event.getRegistration_close())) {
                        eventController.getWaitingList(event.getEventId(), new EventController.WaitingListCallback() {
                            @Override
                            public void onSuccess(ArrayList<Map<String, String>> waitingList) {
                                ArrayList<Entrant> entrants = new ArrayList<>();
                                for (Map<String, String> entry : waitingList) {
                                    // Parse data from waiting list
                                    String name = entry.get("name");
                                    String status = entry.get("status");
                                    String userId = entry.get("userId");
                                    Double latitude = entry.containsKey("latitude") ? Double.valueOf(entry.get("latitude")) : null;
                                    Double longitude = entry.containsKey("longitude") ? Double.valueOf(entry.get("longitude")) : null;

                                    entrants.add(new Entrant(name, status, userId, latitude, longitude));
                                }
                                eventController.getDrawCount(event, new EventController.drawCountCallback() {
                                    @Override
                                    public void onSuccess(int drawCount) {
                                        lotterySystem.drawEntrants(event, entrants, drawCount);
                                        event.setDrawn(true);
                                        eventController.updateField(event, "isDrawn", "true");
                                    }
                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("DrawCountRetrievalError", "Error retrieving draw count: ", e);
                                    }
                                });
                            }
                            @Override
                            public void onFailure(Exception e) {
                                Log.e("WaitlistError", "Error retrieving waitlist: ", e);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Retrieves the selected user.
     * Used for admin functionality.
     *
     * @return The User object marked for deletion.
     */
    public User getSelectedUser() {
        return deleteUser;
    }

    /**
     * Sets the user to delete.
     * Used for admin functionality.
     *
     * @param newUser The user to mark for deletion.
     */
    public void setDeleteUser(User newUser) {
        deleteUser = newUser;
    }

    /**
     * Retrieves the Firebase Storage reference.
     *
     * @return The root Firebase Storage reference.
     */
    public StorageReference getStorageRef() {
        return storageRef;
    }

    /**
     * Callback interface for receiving user data.
     */
    public interface UserLoadCallback {
        void onUserLoaded(User user);
    }
}
