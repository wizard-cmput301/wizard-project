package com.example.wizard_project;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.PhotoHandler;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private User currentUser;
    private User deleteUser;
    private PhotoHandler photo;
    private boolean isAdminMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up view binding and content view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TODO: Delete when done testing
        // addSampleUsersToDatabase();

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // TODO: Delete when done testing
        // ImageView image = findViewById(R.id.event_wizard_logo);
        // photo = new PhotoHandler();
        // photo.loadImage("IMG_0113.JPG",image,this);
        // photo.getUserImage(this);

        // Initialize navigation components
        setupNavigation();

        // Retrieve the device ID, then initialize the user
        String deviceId = retrieveDeviceId();
        initializeUser(deviceId, () -> {
            if (currentUser != null) {
                Toast.makeText(this, "Welcome to EventWizard! ", Toast.LENGTH_SHORT).show();
                setProfilePic();

            } else {
                Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setProfilePic(){
        ImageButton profilePictureButton = findViewById(R.id.profilePictureButton);
        String profilePictureUri = currentUser.getProfilePictureUri();
        if (profilePictureUri != null && !profilePictureUri.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(profilePictureUri))
                    .circleCrop()
                    .into(profilePictureButton);
        } else if (!currentUser.getName().isEmpty()){
            int draw = currentUser.profileGenerator();
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
                    currentUser = new User(deviceId, "", "", false, false, false, "", "", "", "");
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
        userData.put("location", user.getLocation());
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
     * Loads the current user's data from Firestore into the User object.
     * TODO: Delete if no longer needed (currently not used)
     *
     * @param deviceId The unique device ID used as the document ID in Firestore.
     * @param callback The callback to notify when the user is loaded.
     */
    private void loadCurrentUser(String deviceId, UserLoadCallback callback) {
        // Retrieve the user document from Firestore
        db.collection("users").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            // Create and populate a new User object with the retrieved data
            if (documentSnapshot.exists()) {
                currentUser = new User();
                currentUser.setUserData(documentSnapshot);
                callback.onUserLoaded(currentUser);
                // If the user document does not exist, set currentUser to null
            } else {
                currentUser = null;
                callback.onUserLoaded(null);
            }
            // If there was an error retrieving the user document, set currentUser to null
        }).addOnFailureListener(e -> {
            currentUser = null;
            callback.onUserLoaded(null);
        });
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

    // TODO: Delete when done testing
    public void addSampleUsersToDatabase() {
        for (int i = 1; i <= 10; i++) {
            // Create a sample User object
            String id = Integer.toString(i);
            User sampleUsers = new User(id, "@gmail.com", "58888 north ave", false, false, false, "Jerry ", "213123123123", "", "");
            Map<String, Object> userData = createUserDataMap(sampleUsers);
            db.collection("users").document(id).set(userData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "New user created", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        currentUser = null;
                        Toast.makeText(this, "Failed to create user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
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
     * Interface for a callback when the current user is loaded.
     */
    public interface UserLoadCallback {
        void onUserLoaded(User user);
    }
}
