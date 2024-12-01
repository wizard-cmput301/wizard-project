package com.example.wizard_project;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.Manifest;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * AdminUITest tests the functionality of the admin features of the app.
 *
 * <p>Includes UI tests for:</p>
 * <ul>
 *   <li><strong>US 03.01.01</strong>: As an administrator, I want to be able to remove events.</li>
 *   <li><strong>US 03.04.01</strong>: As an administrator, I want to be able to browse events.</li>
 *   <li><strong>US 03.05.01</strong>: As an administrator, I want to be able to browse profiles.</li>
 *   <li><strong>US 03.06.01</strong>: As an administrator, I want to be able to browse images.</li>
 * </ul>
 *
 * <p>The tests use <code>Thread.sleep()</code> for Firebase synchronization delays.
 *
 * <p>Ensure animations are disabled on the test device to avoid test failures.
 * <a href="https://developer.android.com/training/testing/espresso/setup#:~:text=Studio%20is%20recommended.-,Set%20up%20your%20test%20environment,Transition%20animation%20scale">Espresso setup instructions</a></p>
 */
@RunWith(AndroidJUnit4.class)
public class AdminUITest {

    private static String currentUserId;

    // === RULES ===

    /**
     * Grants runtime permissions required for the app.
     */
    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    );

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    // === LIFECYCLE METHODS ===

    @BeforeClass
    public static void setUpClass() {
        // Retrieve the device ID
        currentUserId = retrieveDeviceId();
        if (currentUserId == null) {
            throw new RuntimeException("Device ID could not be retrieved.");
        }

        // Set the admin flag for the user
        updateAdminFlag(true);
    }

    @AfterClass
    public static void tearDownClass() {
        // Reset the admin flag for the user
        updateAdminFlag(false);
    }

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // === HELPER METHODS ===

    /**
     * Retrieves the device ID of the current device.
     *
     * @return The device ID.
     */
    private static String retrieveDeviceId() {
        final String[] deviceId = {null};
        ActivityScenario.launch(MainActivity.class).onActivity(activity ->
                deviceId[0] = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID)
        );
        return deviceId[0];
    }

    /**
     * Updates the admin flag in Firestore for the current user.
     *
     * @param isAdmin True to set as admin, false otherwise.
     */
    private static void updateAdminFlag(boolean isAdmin) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .update("IsAdmin", isAdmin)
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to update admin flag: " + e.getMessage());
                });

        waitForFirestoreSync();
    }

    /**
     * Waits for Firestore to sync changes.
     */
    private static void waitForFirestoreSync() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Fetches the item count from a Firebase Firestore collection.
     *
     * @param collectionName The name of the Firestore collection.
     * @return The number of items in the collection.
     */
    private int getFirebaseCollectionCount(String collectionName) {
        final int[] count = {0};
        FirebaseFirestore.getInstance()
                .collection(collectionName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        count[0] = task.getResult().size();
                    } else {
                        throw new RuntimeException("Failed to fetch items from " + collectionName);
                    }
                });

        waitForFirebase(count);
        return count[0];
    }

    /**
     * Retrieves the number of images stored in Firebase Storage.
     *
     * @return The count of images in Firebase Storage.
     */
    private int getFirebaseImageCount() {
        final int[] count = {0};

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/");

        storageRef.listAll()
                .addOnSuccessListener(listResult -> count[0] = listResult.getItems().size())
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to fetch images from Firebase Storage");
                });

        waitForFirebase(count);
        return count[0];
    }

    /**
     * Waits for Firebase to fetch data before proceeding.
     *
     * @param count Array holding the fetched count.
     */
    private void waitForFirebase(int[] count) {
        while (count[0] == 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Retrieves the number of items currently displayed in a ListView.
     *
     * @param listViewId The resource ID of the ListView.
     * @return The number of items in the ListView's adapter.
     */
    private int getListViewItemCount(int listViewId) {
        final int[] count = {0};

        onView(withId(listViewId)).check((view, noViewFoundException) -> {
            if (view instanceof ListView) {
                ListView listView = (ListView) view;
                count[0] = listView.getAdapter().getCount(); // Get the count from the adapter
            } else {
                throw new RuntimeException("View is not a ListView");
            }
        });

        return count[0];
    }

    /**
     * Adds a test event to Firestore.
     *
     * @return The document ID of the test event.
     */
    private String addTestEvent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Setting the eventId to "1" so it always appears first in the event list
        String eventId = "1";

        Map<String, Object> testEvent = new HashMap<>();
        testEvent.put("eventId", eventId);
        testEvent.put("event_name", "The Red Wedding");
        testEvent.put("event_description", "Join House Frey for a feast like no other!");
        testEvent.put("event_price", 0);
        testEvent.put("event_max_entrants", 100);
        testEvent.put("geolocation_requirement", true);
        testEvent.put("registration_open", new Date());
        testEvent.put("registration_close", new Date(System.currentTimeMillis() + 3600000)); // +1 hour
        testEvent.put("event_location", "Test Files");

        final String[] documentId = {null};
        db.collection("events")
                .document(eventId)
                .set(testEvent)
                .addOnSuccessListener(aVoid -> Log.d("Test", "Test event added successfully: " + eventId))
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to add test event: " + e.getMessage());
                });

        waitForFirestoreSync();
        return eventId;
    }

    /**
     * Checks if an event exists in Firestore.
     *
     * @param eventId The document ID of the event to check.
     * @return True if the event exists, false otherwise.
     */
    private boolean checkEventExistsInFirestore(String eventId) {
        final boolean[] exists = {false};
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> exists[0] = documentSnapshot.exists())
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to check event existence: " + e.getMessage());
                });

        waitForFirestoreSync();
        return exists[0];
    }

    // === TEST METHODS ===

    /**
     * US 03.04.01
     * Ensures that admin can browse events.
     */
    @Test
    public void testBrowseEvents() throws InterruptedException {
        // Wait for Firebase to fetch user data
        waitForFirestoreSync();

        // Navigate to the admin events tab
        Espresso.onView(withId(R.id.admin_button)).perform(click());
        Espresso.onView(withId(R.id.nav_events_browse)).perform(click());
        waitForFirestoreSync();

        // Verify that the event list is displayed
        onView(withId(R.id.event_listview)).check(matches(isDisplayed()));

        // Verify Firebase event count matches UI event count (ie. admins can see all the app's events)
        int firebaseEventCount = getFirebaseCollectionCount("events");
        int uiEventCount = getListViewItemCount(R.id.event_listview);
        assertEquals("Number of events in Firebase does not match the UI", firebaseEventCount, uiEventCount);
    }

    /**
     * US 03.05.01
     * Ensures that admin can browse profiles.
     */
    @Test
    public void testBrowseProfiles() throws InterruptedException {
        // Wait for Firebase to fetch user data
        waitForFirestoreSync();

        // Navigate to the admin profiles tab
        Espresso.onView(withId(R.id.admin_button)).perform(click());
        Espresso.onView(withId(R.id.nav_profile_browse)).perform(click());
        waitForFirestoreSync();

        // Verify that the event list is displayed
        onView(withId(R.id.profilelist_listview)).check(matches(isDisplayed()));

        // Verify Firebase profile count matches UI profile count (ie. admins can see all the app's profiles)
        int firebaseProfileCount = getFirebaseCollectionCount("users");
        int uiProfileCount = getListViewItemCount(R.id.profilelist_listview);
        assertEquals("Number of events in Firebase does not match the UI", firebaseProfileCount, uiProfileCount);
    }

    /**
     * US 03.06.01
     * Ensures that admin can browse images.
     */
    @Test
    public void testBrowseImages() throws InterruptedException {
        // Wait for Firebase to fetch user data
        waitForFirestoreSync();

        // Navigate to the admin images tab
        Espresso.onView(withId(R.id.admin_button)).perform(click());
        Espresso.onView(withId(R.id.nav_image_browse)).perform(click());
        waitForFirestoreSync();

        // Verify that the image list is displayed
        onView(withId(R.id.imageListView)).check(matches(isDisplayed()));

        // Verify Firebase image count matches UI image count
        int firebaseImageCount = getFirebaseImageCount();
        int uiImageCount = getListViewItemCount(R.id.imageListView);
        assertEquals("Number of images in Firebase Storage does not match the UI", firebaseImageCount, uiImageCount);
    }

    /**
     * US 03.06.01
     * Ensures that admin can delete events.
     */
    @Test
    public void testDeleteEvent() throws InterruptedException {
        // Add a test event to Firestore and wait for Firebase to sync
        String testEventId = addTestEvent();
        waitForFirestoreSync();

        // Navigate to the admin events tab
        Espresso.onView(withId(R.id.admin_button)).perform(click());
        Espresso.onView(withId(R.id.nav_events_browse)).perform(click());
        waitForFirestoreSync();

        // Click on the test event, and then click on the delete button
        onView(withText("The Red Wedding")).perform(click());
        onView(withId(R.id.button_delete_event)).perform(click());
        waitForFirestoreSync();

        // Verify the event is no longer in Firestore
        boolean eventExists = checkEventExistsInFirestore(testEventId);
        assertFalse("The test event should be deleted.", eventExists);
    }
}
