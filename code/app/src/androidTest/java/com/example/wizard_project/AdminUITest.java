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
 *   <li><strong>US 03.02.01</strong>: As an administrator, I want to be able to remove profiles.</li>
 *   <li><strong>US 03.03.01</strong>: As an administrator, I want to be able to remove images.</li>
 *   <li><strong>US 03.03.02</strong>: As an administrator, I want to be able to remove hashed QR code data.</li>
 *   <li><strong>US 03.04.01</strong>: As an administrator, I want to be able to browse events.</li>
 *   <li><strong>US 03.05.01</strong>: As an administrator, I want to be able to browse profiles.</li>
 *   <li><strong>US 03.06.01</strong>: As an administrator, I want to be able to browse images.</li>
 *   <li><strong>US 03.07.01</strong>: As an administrator I want to remove facilities that violate app policy.</li>
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

        // Setting a fixed eventId for consistency in tests (event appears at the top of the event list)
        String eventId = "0";

        Map<String, Object> testEvent = new HashMap<>();
        testEvent.put("eventId", eventId);
        testEvent.put("event_name", "The Red Wedding");
        testEvent.put("event_description", "Join House Frey for a feast like no other!");
        testEvent.put("event_price", 0);
        testEvent.put("event_max_entrants", 100);
        testEvent.put("geolocation_requirement", true);
        testEvent.put("registration_open", new Date());
        testEvent.put("registration_close", new Date(System.currentTimeMillis() + 3600000)); // +1 hour
        testEvent.put("event_location", "The Twins");

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

    /**
     * Adds a test profile to Firestore.
     *
     * @return The document ID of the test profile.
     */
    private String addTestProfile() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Setting a fixed deviceID for consistency in tests (profile appears at the top of the profile list)
        String deviceId = "0";

        Map<String, Object> testProfile = new HashMap<>();
        testProfile.put("deviceId", deviceId);
        testProfile.put("name", "Hodor");
        testProfile.put("email", "");
        testProfile.put("phoneNumber", "");
        testProfile.put("IsAdmin", false);
        testProfile.put("IsEntrant", false);
        testProfile.put("isOrganizer", false);
        testProfile.put("photoId", "");
        testProfile.put("profilePath", "");

        final String[] documentId = {null};
        db.collection("users")
                .document(deviceId)
                .set(testProfile)
                .addOnSuccessListener(aVoid -> Log.d("Test", "Test profile added successfully: " + deviceId))
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to add test profile: " + e.getMessage());
                });

        waitForFirestoreSync();
        return deviceId;
    }

    /**
     * Checks if a profile exists in Firestore.
     *
     * @param profileId The document ID of the profile to check.
     * @return True if the profile exists, false otherwise.
     */
    private boolean checkProfileExistsInFirestore(String profileId) {
        final boolean[] exists = {false};
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(profileId).get()
                .addOnSuccessListener(documentSnapshot -> exists[0] = documentSnapshot.exists())
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to check profile existence: " + e.getMessage());
                });

        waitForFirestoreSync();
        return exists[0];
    }

    /**
     * Uploads a test image to Firebase Storage.
     *
     * @return The path of the uploaded test image.
     */
    private String uploadTestImage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images");

        // Setting a fixed image name for consistency in tests (image appears at the top of the image list)
        // NOTE: the image is not always the first item in the list, but I tested it ~20 times and it
        //       was always in the first 5 positions of the list (this means the test never had to scroll
        //       to find the image in the list). If testDeleteImage() fails, it's probably because the
        //       image is not in the first 5 positions of the list.
        String testImageName = "0";
        String testImagePath = "images/" + testImageName;

        byte[] mockImageData = new byte[1024];
        StorageReference testImageRef = storageRef.child(testImageName);

        final boolean[] uploadSuccess = {false};
        testImageRef.putBytes(mockImageData)
                .addOnSuccessListener(taskSnapshot -> uploadSuccess[0] = true)
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to upload test image: " + e.getMessage());
                });

        waitForFirestoreSync();
        return "/" + testImagePath;
    }

    /**
     * Checks if an image exists in Firestore Storage.
     *
     * @param imagePath The image path of the image to check.
     * @return True if the image exists, false otherwise.
     */
    private boolean checkImageExistsInStorage(String imagePath) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReference().child(imagePath.substring(1)); // Remove leading "/"

        final boolean[] exists = {false};
        imageRef.getMetadata()
                .addOnSuccessListener(metadata -> exists[0] = true)
                .addOnFailureListener(e -> exists[0] = false);

        waitForFirestoreSync();
        return exists[0];
    }

    /**
     * Adds a test event to Firestore with a QR code.
     *
     * @return The document ID of the test event.
     */
    private String addTestEventWithQRCode() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Setting a fixed eventId for consistency in tests (event appears at the top of the event list)
        String eventId = "0";

        Map<String, Object> testEvent = new HashMap<>();
        testEvent.put("eventId", eventId);
        testEvent.put("event_name", "Tournament of the Vale");
        testEvent.put("event_description", "Prove your honor and skill in a contest fit for knights and lords!");
        testEvent.put("event_price", 25);
        testEvent.put("event_max_entrants", 64);
        testEvent.put("geolocation_requirement", true);
        testEvent.put("registration_open", new Date());
        testEvent.put("registration_close", new Date(System.currentTimeMillis() + 3600000)); // +1 hour
        testEvent.put("event_location", "The Vale of Arryn");
        testEvent.put("qrCode", "hashed_qr_code_test_data");

        final String[] documentId = {null};
        db.collection("events")
                .document(eventId)
                .set(testEvent)
                .addOnSuccessListener(aVoid -> Log.d("Test", "Test event with QR code added successfully: " + eventId))
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to add test event: " + e.getMessage());
                });

        waitForFirestoreSync();
        return eventId;
    }

    /**
     * Checks if the QR code data exists in Firestore for a given event.
     *
     * @param eventId The document ID of the event to check.
     * @return True if the QR code exists, false otherwise.
     */
    private boolean checkQRCodeExistsInFirestore(String eventId) {
        final boolean[] exists = {false};
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String qrCode = documentSnapshot.getString("qrCode");
                        exists[0] = qrCode != null && !qrCode.isEmpty();
                    }
                })
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to check QR code existence: " + e.getMessage());
                });

        waitForFirestoreSync();
        return exists[0];
    }

    /**
     * Adds a test facility to Firestore.
     *
     * @return The document ID of the test facility.
     */
    private String addTestFacility() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Setting a fixed facilityId for consistency in tests (facility appears at the top of the facility list)
        String facilityId = "0";

        Map<String, Object> testFacility = new HashMap<>();
        testFacility.put("userId", "");
        testFacility.put("facilityId", facilityId);
        testFacility.put("name", "Castle Black");
        testFacility.put("location", "The Wall");
        testFacility.put("facility_imagePath", "");
        testFacility.put("posterUri", "");

        db.collection("facilities").document(facilityId)
                .set(testFacility)
                .addOnSuccessListener(aVoid -> Log.d("Test", "Test facility added: " + facilityId))
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to add test facility: " + e.getMessage());
                });

        waitForFirestoreSync();
        return facilityId;
    }

    /**
     * Checks if a facility exists in Firestore.
     *
     * @param facilityId The document ID of the facility to check.
     * @return True if the facility exists, false otherwise.
     */
    private boolean checkFacilityExistsInFirestore(String facilityId) {
        final boolean[] exists = {false};
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("facilities").document(facilityId).get()
                .addOnSuccessListener(documentSnapshot -> exists[0] = documentSnapshot.exists())
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to check facility existence: " + e.getMessage());
                });

        waitForFirestoreSync();
        return exists[0];
    }

    /**
     * Deletes a test event from Firestore.
     *
     * @param eventId The document ID of the event to delete.
     */
    private void deleteTestEvent(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> Log.d("Test", "Test event deleted successfully: " + eventId))
                .addOnFailureListener(e -> Log.e("Test", "Failed to delete test event: " + e.getMessage()));
    }

    // === TEST METHODS ===

    /**
     * US 03.01.01
     * Ensures that admin can remove events.
     */
    @Test
    public void testRemoveEvent() throws InterruptedException {
        // Add a test event to Firestore and wait for Firebase to sync
        String testEventId = addTestEvent();
        waitForFirestoreSync();

        // Navigate to the admin events tab
        Espresso.onView(withId(R.id.admin_button)).perform(click());
        Espresso.onView(withId(R.id.nav_events_browse)).perform(click());
        waitForFirestoreSync();

        // Click on the test event, and then click on the delete button
        onView(withText("The Red Wedding")).perform(click());
        waitForFirestoreSync();
        onView(withId(R.id.button_delete_event)).perform(click());
        waitForFirestoreSync();

        // Verify the event is no longer in Firestore
        boolean eventExists = checkEventExistsInFirestore(testEventId);
        assertFalse("The test event should be deleted.", eventExists);
    }

    /**
     * US 03.02.01
     * Ensures that admin can remove profiles.
     */
    @Test
    public void testRemoveProfile() throws InterruptedException {
        // Add a test profile to Firestore and wait for Firebase to sync
        String testProfileId = addTestProfile();
        waitForFirestoreSync();

        // Navigate to the admin profiles tab
        Espresso.onView(withId(R.id.admin_button)).perform(click());
        Espresso.onView(withId(R.id.nav_profile_browse)).perform(click());
        waitForFirestoreSync();

        // Click on the test profile, and then click on the delete button
        onView(withText("Hodor")).perform(click());
        waitForFirestoreSync();
        onView(withId(R.id.button_delete_profile)).perform(click());
        waitForFirestoreSync();

        // Verify the profile is no longer in Firestore
        boolean profileExists = checkProfileExistsInFirestore(testProfileId);
        assertFalse("The test profile should be deleted.", profileExists);
    }

    /**
     * US 03.03.01
     * Ensures that admin can remove images.
     */
    @Test
    public void testRemoveImage() throws InterruptedException {
        // Add a test image to Firestore and wait for Firebase to sync
        String testImagePath = uploadTestImage();
        waitForFirestoreSync();

        // Navigate to the admin images tab
        Espresso.onView(withId(R.id.admin_button)).perform(click());
        Espresso.onView(withId(R.id.nav_image_browse)).perform(click());
        waitForFirestoreSync();

        // Click on the test image, and then confirm the deletion in the alert dialog
        onView(withText(testImagePath)).perform(click());
        waitForFirestoreSync();
        onView(withText("Yes")).perform(click());
        waitForFirestoreSync();

        // Verify the image is no longer in Firestore
        boolean imageExists = checkImageExistsInStorage(testImagePath);
        assertFalse("The test image should be deleted.", imageExists);
    }

    /**
     * US 03.03.02
     * Ensures that admin can remove hashed QR code data.
     */
    @Test
    public void testRemoveQRData() throws InterruptedException {
        // Add a test event with a QR code to Firestore and wait for Firebase to sync
        String testEventId = addTestEventWithQRCode();
        waitForFirestoreSync();

        try {
            // Navigate to the admin events tab
            Espresso.onView(withId(R.id.admin_button)).perform(click());
            Espresso.onView(withId(R.id.nav_events_browse)).perform(click());
            waitForFirestoreSync();

            // Click on the test event, and then click on the delete QR Code data button
            onView(withText("Tournament of the Vale")).perform(click());
            waitForFirestoreSync();
            onView(withId(R.id.button_delete_event_qr_data)).perform(click());
            waitForFirestoreSync();

            // Verify the event is no longer in Firestore
            boolean QRCodeDataExists = checkQRCodeExistsInFirestore(testEventId);
            assertFalse("The test event's QR code data should be deleted.", QRCodeDataExists);

        } finally {
            // Delete the test event from Firestore
            deleteTestEvent(testEventId);
            waitForFirestoreSync();
        }
    }

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
     * US 03.07.01
     * Ensures that admin can remove facilities.
     */
    @Test
    public void testRemoveFacility() throws InterruptedException {
        // Add a test facility to Firestore and wait for Firebase to sync
        String testFacilityId = addTestFacility();
        waitForFirestoreSync();

        // Navigate to the admin facilities tab
        Espresso.onView(withId(R.id.admin_button)).perform(click());
        Espresso.onView(withId(R.id.nav_facility_browse)).perform(click());
        waitForFirestoreSync();

        // Click on the test facility, and then click on the delete button
        onView(withText("Castle Black")).perform(click());
        waitForFirestoreSync();
        onView(withId(R.id.button_delete_facility)).perform(click());
        waitForFirestoreSync();

        // Verify the facility is no longer in Firestore
        boolean facilityExists = checkFacilityExistsInFirestore(testFacilityId);
        assertFalse("The test facility should be deleted.", facilityExists);
    }
}
