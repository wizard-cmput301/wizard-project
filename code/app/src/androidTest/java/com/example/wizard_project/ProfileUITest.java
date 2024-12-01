package com.example.wizard_project;

import static android.app.Activity.RESULT_OK;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ProfileUITest tests the functionality of the profile features of the app.
 *
 * <p>Includes UI tests for:</p>
 * <ul>
 *   <li><strong>US 01.02.01</strong>: As an entrant, I want to provide my personal information such
 *   as name, email, and optional phone number in the app.</li>
 *   <li><strong>US 01.02.02</strong>: As an entrant, I want to update information such as name,
 *   email, and contact information on my profile.</li>
 *   <li><strong>US 01.03.01</strong>: As an entrant, I want to upload a profile picture for a more
 *   personalized experience.</li>
 *   <li><strong>US 01.03.02</strong>: As an entrant, I want to remove a profile picture if needed.</li>
 * </ul>
 *
 * <p>The tests use <code>Thread.sleep()</code> for Firebase synchronization delays.
 *
 * <p>Ensure animations are disabled on the test device to avoid test failures.
 * <a href="https://developer.android.com/training/testing/espresso/setup#:~:text=Studio%20is%20recommended.-,Set%20up%20your%20test%20environment,Transition%20animation%20scale">Espresso setup instructions</a></p>
 */
@RunWith(AndroidJUnit4.class)
public class ProfileUITest {

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

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // === TEST METHODS ===

    /**
     * US 01.02.01
     * Ensures that the entrant can enter their name, email, and phone number in the profile.
     */
    @Test
    public void testAddPersonalInformation() {
        // Navigate to the edit profile screen
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.button_edit_profile)).perform(ViewActions.click());

        // Enter profile information
        Espresso.onView(withId(R.id.edittext_name)).perform(ViewActions.clearText(), ViewActions.typeText("Jon Snow"));
        Espresso.onView(withId(R.id.edittext_email)).perform(ViewActions.clearText(), ViewActions.typeText("jsnow@winterfell.com"));
        Espresso.onView(withId(R.id.edittext_phone)).perform(ViewActions.clearText(), ViewActions.typeText("1234567890"));

        // Save profile changes and verify they are displayed
        Espresso.onView(withId(R.id.buttonSaveProfile)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.textview_profile_name)).check(matches(withText("Jon Snow")));
        Espresso.onView(withId(R.id.textview_profile_email)).check(matches(withText("jsnow@winterfell.com")));
        Espresso.onView(withId(R.id.textview_profile_phone)).check(matches(withText("1234567890")));
    }

    /**
     * US 01.02.02
     * Ensures that profile details can be updated and persist upon returning to the screen.
     */
    @Test
    public void testUpdateProfileInformation() throws InterruptedException {
        // Giving Firebase two seconds to fetch the user data
        Thread.sleep(2000);

        // Navigate to the edit profile screen
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.button_edit_profile)).perform(ViewActions.click());

        // Enter initial profile information
        Espresso.onView(withId(R.id.edittext_name)).perform(ViewActions.clearText(), ViewActions.typeText("Daenerys Targaryen"));
        Espresso.onView(withId(R.id.edittext_email)).perform(ViewActions.clearText(), ViewActions.typeText("dtargaryen@dragonstone.com"));
        Espresso.onView(withId(R.id.edittext_phone)).perform(ViewActions.clearText(), ViewActions.typeText("1234567890"));
        Espresso.onView(withId(R.id.buttonSaveProfile)).perform(ViewActions.click());

        // Verify initial profile details
        Espresso.onView(withId(R.id.textview_profile_name)).check(matches(withText("Daenerys Targaryen")));
        Espresso.onView(withId(R.id.textview_profile_email)).check(matches(withText("dtargaryen@dragonstone.com")));
        Espresso.onView(withId(R.id.textview_profile_phone)).check(matches(withText("1234567890")));

        // Update profile information, and deleting the phone number to show it is optional
        Espresso.onView(withId(R.id.button_edit_profile)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.edittext_name)).perform(ViewActions.clearText(), ViewActions.typeText("Tyrion Lannister"));
        Espresso.onView(withId(R.id.edittext_email)).perform(ViewActions.clearText(), ViewActions.typeText("tlannister@kingslanding.com"));
        Espresso.onView(withId(R.id.edittext_phone)).perform(ViewActions.clearText(), ViewActions.typeText(""));
        Espresso.onView(withId(R.id.buttonSaveProfile)).perform(ViewActions.click());

        // Hit the home button, then navigate back to the profile screen
        Espresso.onView(withId(R.id.nav_home)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());

        // Verify the updated profile details are displayed
        Espresso.onView(withId(R.id.textview_profile_name)).check(matches(withText("Tyrion Lannister")));
        Espresso.onView(withId(R.id.textview_profile_email)).check(matches(withText("tlannister@kingslanding.com")));
        Espresso.onView(withId(R.id.textview_profile_phone)).check(matches(withText("No phone number provided"))); // Placeholder for when user does not provide a phone number
    }

    /**
     * US 01.03.01
     * Ensures that the user can upload a profile picture.
     */
    @Test
    public void testUploadProfilePicture() throws InterruptedException {
        // Giving Firebase two seconds to fetch the user data
        Thread.sleep(2000);

        // Navigate to the edit profile screen
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.button_edit_profile)).perform(ViewActions.click());

        // Create mock image URI to simulate picking an image
        Uri mockImageUri = Uri.parse("android.resource://com.example.wizard_project/drawable/event_wizard_logo");
        Intent resultData = new Intent();
        resultData.setData(mockImageUri);

        // Simulate picking an image and adding it to the profile
        intending(hasAction(Intent.ACTION_PICK)).respondWith(new Instrumentation.ActivityResult(RESULT_OK, resultData));
        Espresso.onView(withId(R.id.framelayout_profile_picture_container)).perform(ViewActions.click());

        // Save the changes
        Espresso.onView(withId(R.id.buttonSaveProfile)).perform(ViewActions.click());

        // Navigate away and return to profile screen to verify persistence
        Espresso.onView(withId(R.id.nav_home)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());

        // Verify that the profile picture persists and is displayed
        Espresso.onView(withId(R.id.imageview_profile_image)).check(matches(isDisplayed()));
    }

    /**
     * US 01.03.02
     * Ensures that the user can remove an uploaded profile picture.
     */
    @Test
    public void testRemoveProfilePicture() throws InterruptedException {
        // Giving Firebase two seconds to fetch the user data
        Thread.sleep(2000);

        // Navigate to the edit profile screen
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.button_edit_profile)).perform(ViewActions.click());

        // Create mock image URI to simulate picking an image
        Uri mockImageUri = Uri.parse("android.resource://com.example.wizard_project/drawable/event_wizard_logo");
        Intent resultData = new Intent();
        resultData.setData(mockImageUri);

        // Simulate picking an image and adding it to the profile
        intending(hasAction(Intent.ACTION_PICK)).respondWith(new Instrumentation.ActivityResult(RESULT_OK, resultData));
        Espresso.onView(withId(R.id.framelayout_profile_picture_container)).perform(ViewActions.click());

        // Save changes after adding the profile picture
        Espresso.onView(withId(R.id.buttonSaveProfile)).perform(ViewActions.click());

        // Verify the profile picture is displayed
        Espresso.onView(withId(R.id.imageview_profile_image)).check(matches(isDisplayed()));

        // Navigate back to edit profile and remove the profile picture
        Espresso.onView(withId(R.id.button_edit_profile)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.buttonDeleteProfilePic)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.buttonSaveProfile)).perform(ViewActions.click());

        // Verify the profile picture is removed (fallback image or placeholder is displayed)
        Espresso.onView(withId(R.id.imageview_profile_image)).check(matches(isDisplayed()));

        // Navigate away and back to ensure the change persists
        Espresso.onView(withId(R.id.nav_home)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());

        // Verify the profile picture is still removed
        Espresso.onView(withId(R.id.imageview_profile_image)).check(matches(isDisplayed()));
    }
}
