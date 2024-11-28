package com.example.wizard_project;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static android.app.Activity.RESULT_OK;

/**
 * ProfileFragmentTest tests the functionality of the profile features of the app.
 * Includes tests for:
 * US 01.02.01 As an entrant, I want to provide my personal information such as name, email and
 *             optional phone number in the app
 * US 01.02.02 As an entrant I want to update information such as name, email and contact
 *             information on my profile
 * US 01.03.01 As an entrant I want to upload a profile picture for a more personalized experience
 * US 01.03.02 As an entrant I want remove profile picture if need be
 * TODO: Add tests for US 01.03.03 As an entrant I want my profile picture to be deterministically generated from my profile name if I haven't uploaded a profile image yet (if possible)
 * TODO: Fix US 01.03.02, currently failing
 * TODO: Do i need to grant permission for location sharing in these?
 */
@RunWith(AndroidJUnit4.class)
public class ProfileFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * US 01.02.01
     * Ensures that the entrant can enter their name, email, and phone number in the profile.
     */
    @Test
    public void testAddPersonalInformation() {
        // Navigate to the profile screen
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.button_edit_profile)).perform(ViewActions.click());

        // Enter personal information
        Espresso.onView(withId(R.id.edittext_name)).perform(ViewActions.clearText(), ViewActions.typeText("New Name"));
        Espresso.onView(withId(R.id.edittext_email)).perform(ViewActions.clearText(), ViewActions.typeText("newemail@example.com"));
        Espresso.onView(withId(R.id.edittext_phone)).perform(ViewActions.clearText(), ViewActions.typeText("0987654321"));

        // Save profile changes and verify they are displayed
        Espresso.onView(withId(R.id.buttonSaveProfile)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.ProfileFragment)).check(matches(withText("New Name")));
        Espresso.onView(withId(R.id.ProfileFragment)).check(matches(withText("newemail@example.com")));
    }

    /**
     * US 01.02.02
     * Ensures that profile details can be updated and persist upon returning to the screen.
     */
    @Test
    public void testUpdateProfileInformation() {
        // Navigate to the profile screen
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.EditProfileFragment)).perform(ViewActions.click());

        // Update profile details
        Espresso.onView(withId(R.id.edittext_name)).perform(ViewActions.clearText(), ViewActions.typeText("Updated Name"));
        Espresso.onView(withId(R.id.edittext_email)).perform(ViewActions.clearText(), ViewActions.typeText("updatedemail@example.com"));
        Espresso.onView(withId(R.id.buttonSaveProfile)).perform(ViewActions.click());

        // Hit the home button, then navigate back to the profile screen
        Espresso.onView(withId(R.id.nav_home)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());

        // Verify the updated profile details are displayed
        Espresso.onView(withId(R.id.ProfileFragment)).check(matches(withText("Updated Name")));
        Espresso.onView(withId(R.id.ProfileFragment)).check(matches(withText("updatedemail@example.com")));
    }

    /**
     * US 01.03.01
     * Ensures that the user can upload a profile picture.
     */
    @Test
    public void testUploadProfilePicture() {
        // Navigate to the profile screen
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.EditProfileFragment)).perform(ViewActions.click());

        // Create mock image URI to simulate picking an image
        Uri mockImageUri = Uri.parse("android.resource://com.example.wizard_project/drawable/event_wizard_logo");
        Intent resultData = new Intent();
        resultData.setData(mockImageUri);

        // Simulate picking an image and adding it to the profile
        intending(hasAction(Intent.ACTION_PICK)).respondWith(new Instrumentation.ActivityResult(RESULT_OK, resultData));
        Espresso.onView(withId(R.id.EditProfileFragment)).perform(ViewActions.click());

        // Verify that the profile picture is displayed
        Espresso.onView(withId(R.id.EditProfileFragment)).check(matches(isDisplayed()));
    }

    /**
     * US 01.03.02
     * Ensures that the user can remove an uploaded profile picture.
     * TODO: Fix after implementing US 01.03.03 As an entrant I want my profile picture to be deterministically generated from my profile name if I haven't uploaded a profile image yet
     */
    @Test
    public void testRemoveProfilePicture() {
        // Navigate to the profile screen
        Espresso.onView(withId(R.id.enter_event_button)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.profilePictureButton)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.EditProfileFragment)).perform(ViewActions.click());

        // Delete the current profile picture
        Espresso.onView(withId(R.id.buttonDeleteProfilePic)).perform(ViewActions.click());

        // Verify that a placeholder or default image is displayed instead of the removed profile picture
        Espresso.onView(withId(R.id.EditProfileFragment)).check(matches(isDisplayed()));
    }

     // TODO: US 01.03.03: Add test for deterministically generated profile picture based on profile name if no image is uploaded.
}
