package com.example.wizard_project;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;

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
 * OrganizerUITest tests the functionality of the organizer features of the app.
 *
 * <p>Includes UI tests for:</p>
 * <ul>
 *   <li><strong>US 02.01.03</strong>: As an organizer, I want to create and manage my facility profile.</li>
 * </ul>
 *
 * <p>Ensure animations are disabled on the test device to avoid test failures.
 * <a href="https://developer.android.com/training/testing/espresso/setup#:~:text=Studio%20is%20recommended.-,Set%20up%20your%20test%20environment,Transition%20animation%20scale">Espresso setup instructions</a></p>
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerUITest {

    // === RULES ===

    /**
     * Grants runtime permissions required for location-based features in tests.
     */
    @Rule
    public GrantPermissionRule grantPermissionRule =
            GrantPermissionRule.grant(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
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

    // === TEST METHODS ===
    /**
     * US 02.01.03
     * Ensures that a user is able to create and manage a facility profile.
     */
    @Test
    public void testBrowseEvents() throws InterruptedException {
        // Giving Firebase two seconds to fetch the user data
        Thread.sleep(2000);

        // Navigate to the facility screen
        Espresso.onView(withId(R.id.manage_facility_button)).perform(ViewActions.click());

        // Check if the user has already made a facility profile
        try {
            // If the facility exists, navigate to the edit facility screen
            Espresso.onView(withId(R.id.textview_facility_name)).check(matches(isDisplayed()));
            Espresso.onView(withId(R.id.button_edit_facility)).perform(ViewActions.click());
        } catch (Exception e) {
            // If the facility does not exist, stay on the EditFacilityFragment to create one
        }

        // Enter facility profile information
        Espresso.onView(withId(R.id.edittext_name)).perform(ViewActions.clearText(), ViewActions.typeText("Red Keep"));
        Espresso.onView(withId(R.id.edittext_facility_location)).perform(ViewActions.clearText(), ViewActions.typeText("King's Landing"));
        Espresso.onView(withId(R.id.button_save_facility)).perform(ViewActions.click());

        // Giving Firebase two seconds to save the facility data
        Thread.sleep(2000);

        // Verify that the facility details are displayed
        Espresso.onView(withId(R.id.textview_facility_name)).check(matches(withText("Red Keep")));
        Espresso.onView(withId(R.id.textview_facility_location)).check(matches(withText("King's Landing")));

        // Hit the home button, then navigate back to the facility profile screen
        Espresso.onView(withId(R.id.nav_home)).perform(ViewActions.click());
        Espresso.onView(withId(R.id.manage_facility_button)).perform(ViewActions.click());

        // Verify the updated profile details are still displayed
        Espresso.onView(withId(R.id.textview_facility_name)).check(matches(withText("Red Keep")));
        Espresso.onView(withId(R.id.textview_facility_location)).check(matches(withText("King's Landing")));
    }









}
