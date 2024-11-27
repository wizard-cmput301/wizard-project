package com.example.wizard_project.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.WaitingListController;
import com.example.wizard_project.Controllers.EventController;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentViewEventBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;

/**
 * ViewEventFragment displays an event's information and adjusts the UI based on the user's role:
 * - Entrant: Can view an event they scanned through a QR code, view the event's details, and join it's waitlist
 * - Organizer: Can view and edit their own events, and manage the event's waitlist.
 * - Admin: Can view and delete selected events from the admin event list, and delete the QR code data for the event.
 */
public class ViewEventFragment extends Fragment {
    private final EventController controller = new EventController();
    private FragmentViewEventBinding binding;
    private User currentUser; // The current logged-in user
    private Event displayEvent; // The event being viewed
    private WaitingListController waitingListController; //

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewEventBinding.inflate(inflater, container, false);

        // Set up navigation to ProfileFragment when the profile picture button is clicked
        View profilePictureButton = requireActivity().findViewById(R.id.profilePictureButton);

        profilePictureButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.ProfileFragment) {
                navController.navigate(R.id.action_ViewEventFragment_to_ProfileFragment); // (temporary work around, this prevents app crashing when clicking the button twice)
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize waiting list controller
        waitingListController = new WaitingListController();

        // Get the current user from MainActivity
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();

        // Retrieve the event passed to this fragment
        displayEvent = (Event) getArguments().getSerializable("event");

        if (displayEvent != null) {
            bindEventData(displayEvent);
            configureViewBasedOnRole(view);
        } else {
            Toast.makeText(requireContext(), "Event data unavailable", Toast.LENGTH_SHORT).show();
        }

        // Retrieve the event passed from QRScannerFragment
        if (getArguments() != null) {
            displayEvent = (Event) getArguments().getSerializable("event");
        }
    }

    /**
     * Binds the event data to the UI elements.
     *
     * @param event The event object containing the data.
     */
    private void bindEventData(Event event) {
        binding.textviewEventName.setText(event.getEvent_name());
        binding.textviewEventDescription.setText(event.getEvent_description());
        binding.textviewEventPrice.setText(String.format("$%s", event.getEvent_price()));
        binding.textviewMaxEntrants.setText(String.valueOf(event.getEvent_max_entrants()));

        binding.textviewGeolocationRequirement.setText(
                event.isGeolocation_requirement() ? "Geolocation required" : "No geolocation required"
        );

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String registrationDates = dateFormat.format(event.getRegistration_open()) +
                " - " + dateFormat.format(event.getRegistration_close());
        binding.textviewRegistrationDates.setText(registrationDates);

        if (event.getPosterUri() != null) {
            Glide.with(requireContext())
                    .load(Uri.parse(event.getPosterUri()))
                    .into(binding.imageviewEventImage);
        } else {
            binding.imageviewEventImage.setImageResource(R.drawable.example_event);
        }
    }

    /**
     * Configures the UI based on the user's role and the source of navigation.
     *
     * @param view The root view of the fragment.
     */
    private void configureViewBasedOnRole(View view) {
        NavController navController = Navigation.findNavController(view);
        int previousDestinationId = navController.getPreviousBackStackEntry().getDestination().getId();

        if (previousDestinationId == R.id.AdminFragmentEventView && currentUser.isAdmin()) {
            setupAdminView(navController);
        } else if (previousDestinationId == R.id.EventListFragment && currentUser.isOrganizer()) {
            setupOrganizerView(navController);
        } else {
            setupEntrantView();
        }
    }

    /**
     * Configures the UI for admin users.
     *
     * @param navController The NavController for navigation.
     */
    private void setupAdminView(NavController navController) {
        binding.buttonDeleteEvent.setVisibility(View.VISIBLE);
        binding.buttonDeleteEvent.setOnClickListener(v -> deleteEvent(navController));

        binding.buttonDeleteEventQrData.setVisibility(View.VISIBLE);

        hideUnusedButtonsForAdmin();
    }

    /**
     * Configures the UI for organizers.
     *
     * @param navController The NavController for navigation.
     */
    private void setupOrganizerView(NavController navController) {
        binding.buttonViewEntrants.setVisibility(View.VISIBLE);
        binding.buttonViewQrCode.setVisibility(View.VISIBLE);
        binding.buttonViewMap.setVisibility(View.VISIBLE);
        binding.buttonEditEvent.setVisibility(View.VISIBLE);

        // Set up the view entrants button
        binding.buttonViewEntrants.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", displayEvent); // Pass the current event
            navController.navigate(R.id.action_ViewEventFragment_to_EntrantListFragment, bundle);
        });

        // Set up the view QR button
        binding.buttonViewQrCode.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", displayEvent.getEventId()); // Pass event ID
            navController.navigate(R.id.action_ViewEventFragment_to_ViewQRCodeFragment, bundle);
        });

        binding.buttonEditEvent.setOnClickListener(v -> navigateToEditEvent(navController));

        setupBottomNavigationForOrganizer(navController);
    }

    /**
     * Configures the UI for entrants.
     */
    private void setupEntrantView() {

        hideUnusedButtonsForEntrant();

        // Use WaitingListController to check waiting list status
        waitingListController.isUserOnWaitingList(displayEvent.getEventId(), currentUser.getDeviceId(), new WaitingListController.OnCheckCompleteListener() {
            @Override
            public void onComplete(boolean isOnWaitingList) {
                if (isOnWaitingList) {
                    binding.buttonLeaveWaitingList.setVisibility(View.VISIBLE);
                    binding.buttonJoinWaitingList.setVisibility(View.GONE);
                } else {
                    binding.buttonJoinWaitingList.setVisibility(View.VISIBLE);
                    binding.buttonLeaveWaitingList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ViewEventFragment", "Error checking waiting list status", e);
                Toast.makeText(requireContext(), "Unable to check waiting list status.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up listeners for join and leave buttons
        binding.buttonJoinWaitingList.setOnClickListener(v -> joinWaitingList());
        binding.buttonLeaveWaitingList.setOnClickListener(v -> leaveWaitingList());
    }


    /**
     * Places the current user to the waitlist in the Firestore.
     *
     */

    private void joinWaitingList() {
        waitingListController.addUserToWaitingList(displayEvent.getEventId(), currentUser, currentUser.getDeviceId(), new WaitingListController.OnActionCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "You have joined the waiting list.", Toast.LENGTH_SHORT).show();
                binding.buttonJoinWaitingList.setVisibility(View.GONE);
                binding.buttonLeaveWaitingList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to join the waiting list.", Toast.LENGTH_SHORT).show();
                Log.e("ViewEventFragment", "Error joining waiting list", e);
            }
        });
    }

    /**
     * Deletes the current user from the waitlist in the Firestore.
     *
     */

    private void leaveWaitingList() {
        waitingListController.removeUserFromWaitingList(displayEvent.getEventId(), currentUser.getDeviceId(), new WaitingListController.OnActionCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "You have left the waiting list.", Toast.LENGTH_SHORT).show();
                binding.buttonLeaveWaitingList.setVisibility(View.GONE);
                binding.buttonJoinWaitingList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to leave the waiting list.", Toast.LENGTH_SHORT).show();
                Log.e("ViewEventFragment", "Error leaving waiting list", e);
            }
        });
    }


    /**
     * Deletes the current event from Firestore.
     *
     * @param navController The NavController for navigation after deletion.
     */
    private void deleteEvent(NavController navController) {
        controller.deleteEvent(displayEvent.getEventId(), new EventController.deleteCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show();
                navController.popBackStack();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error deleting event", Toast.LENGTH_SHORT).show();
                Log.e("ViewEventFragment", "Event deletion failed", e);
            }
        });
    }

    /**
     * Navigates to the EditEventFragment to edit the current event.
     *
     * @param navController The NavController for navigation.
     */
    private void navigateToEditEvent(NavController navController) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", displayEvent);
        navController.navigate(R.id.action_ViewEventFragment_to_EditEventFragment, bundle);
    }

    /**
     * Sets up the bottom navigation for organizers.
     *
     * @param navController The NavController for navigation.
     */
    private void setupBottomNavigationForOrganizer(NavController navController) {
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.organizer_nav_menu);

        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                navController.navigate(R.id.HomeFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_add_event) {
                navController.navigate(R.id.EditEventFragment);
                return true;
            } else {
                navController.navigate(R.id.EventListFragment);
                return true;
            }
        });
    }

    /**
     * Hides buttons that are not used by admin users.
     */
    private void hideUnusedButtonsForAdmin() {
        binding.buttonViewEntrants.setVisibility(View.GONE);
        binding.buttonViewFacility.setVisibility(View.GONE);
        binding.buttonViewQrCode.setVisibility(View.GONE);
        binding.buttonViewMap.setVisibility(View.GONE);
        binding.buttonEditEvent.setVisibility(View.GONE);
    }

    /**
     * Hides buttons that are not used by entrants.
     */
    private void hideUnusedButtonsForEntrant() {
        binding.buttonViewQrCode.setVisibility(View.GONE);
        binding.buttonViewMap.setVisibility(View.GONE);
        binding.buttonEditEvent.setVisibility(View.GONE);
        binding.buttonDeleteEvent.setVisibility(View.GONE);
        binding.buttonDeleteEventQrData.setVisibility(View.GONE);
        binding.buttonViewEntrants.setVisibility(View.GONE);
        binding.buttonViewFacility.setVisibility(View.GONE);

    }
}
