package com.example.wizard_project;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import android.Manifest;
import android.widget.ListView;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * AdminUITest tests the functionality of the admin features of the app.
 *
 * <p>Includes UI tests for:</p>
 * <ul>
 *   <li><strong>US 03.04.01</strong>: As an administrator, I want to be able to browse events.</li>
 *   <li><strong>US 03.05.01</strong>: As an administrator, I want to be able to browse profiles.</li>
 *   <li><strong>US 03.06.01</strong>: As an administrator, I want to be able to browse images.</li>
 * </ul>
 *
 * <p>For admin tests to pass, the test device must have <code>isAdmin = True</code> set in Firestore.<br>
 * The tests temporarily use <code>Thread.sleep()</code> for Firebase synchronization delays.
 *
 * <p>Ensure animations are disabled on the test device to avoid test failures.
 * <a href="https://developer.android.com/training/testing/espresso/setup#:~:text=Studio%20is%20recommended.-,Set%20up%20your%20test%20environment,Transition%20animation%20scale">Espresso setup instructions</a></p>
 */
@RunWith(AndroidJUnit4.class)
public class AdminUITest {

    // === RULES ===

    /**
     * Grants runtime permissions required for location-based features in tests.
     */
    @Rule
    public GrantPermissionRule grantPermissionRule =
            GrantPermissionRule.grant(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            );

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    // === LIFECYCLE METHODS ===

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
     * Retrieves the number of events stored in Firebase.
     *
     * @return The count of events in Firebase.
     */
    private int getFirebaseEventCount() {
        final int[] count = {0};

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        count[0] = task.getResult().size();
                    } else {
                        throw new RuntimeException("Failed to fetch events from Firebase");
                    }
                });

        waitForFirebase(count);
        return count[0];
    }

    /**
     * Retrieves the number of profiles stored in Firebase.
     *
     * @return The count of profiles in Firebase.
     */
    private int getFirebaseProfileCount() {
        final int[] count = {0};

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        count[0] = task.getResult().size();
                    } else {
                        throw new RuntimeException("Failed to fetch profiles from Firebase");
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

    // === TEST METHODS ===

    /**
     * US 03.04.01
     * Ensures that admin can browse events.
     */
    @Test
    public void testBrowseEvents() throws InterruptedException {
        // Giving Firebase two seconds to fetch the user data, so that the admin button is displayed
        Thread.sleep(2000);

        // Navigate to the admin events tab
        Espresso.onView(withId(R.id.admin_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.nav_events_browse)).perform(ViewActions.click());

        // Wait for Firebase to fetch events
        Thread.sleep(2000);

        // Verify that the event list is displayed
        onView(withId(R.id.event_listview)).check(matches(isDisplayed()));

        // Verify Firebase event count matches UI event count (ie. admins can see all the app's events)
        int firebaseEventCount = getFirebaseEventCount();
        int uiEventCount = getListViewItemCount(R.id.event_listview);
        assertEquals("Number of events in Firebase does not match the UI", firebaseEventCount, uiEventCount);
    }

    /**
     * US 03.05.01
     * Ensures that admin can browse profiles.
     */
    @Test
    public void testBrowseProfiles() throws InterruptedException {
        // Giving Firebase two seconds to fetch the user data, so that the admin button is displayed
        Thread.sleep(2000);

        // Navigate to the admin profiles tab
        Espresso.onView(withId(R.id.admin_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.nav_profile_browse)).perform(ViewActions.click());

        // Wait for Firebase to fetch profiles
        Thread.sleep(2000);

        // Verify that the event list is displayed
        onView(withId(R.id.profilelist_listview)).check(matches(isDisplayed()));

        // Verify Firebase profile count matches UI profile count (ie. admins can see all the app's profiles)
        int firebaseProfileCount = getFirebaseProfileCount();
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
        Thread.sleep(2000);

        // Navigate to the admin images tab
        Espresso.onView(withId(R.id.admin_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.nav_image_browse)).perform(ViewActions.click());

        // Wait for Firebase to fetch images
        Thread.sleep(2000);

        // Verify that the image list is displayed
        onView(withId(R.id.imageListView)).check(matches(isDisplayed()));

        // Verify Firebase image count matches UI image count
        int firebaseImageCount = getFirebaseImageCount();
        int uiImageCount = getListViewItemCount(R.id.imageListView);
        assertEquals("Number of images in Firebase Storage does not match the UI", firebaseImageCount, uiImageCount);
    }
}
