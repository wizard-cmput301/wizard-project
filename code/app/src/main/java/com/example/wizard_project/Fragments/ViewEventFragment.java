package com.example.wizard_project.Fragments;

import static com.example.wizard_project.MainActivity.LOCATION_PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.LatLng;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.EventController;
import com.example.wizard_project.Controllers.WaitingListController;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentViewEventBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
    private WaitingListController waitingListController;
    private FusedLocationProviderClient locationProvider;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize waiting list controller
        waitingListController = new WaitingListController();

        // Initialize location provider
        locationProvider = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Get the event passed to this fragment
        displayEvent = (Event) getArguments().getSerializable("event");
        if (displayEvent == null) {
            Toast.makeText(requireContext(), "Event data is missing.", Toast.LENGTH_SHORT).show();
            return; // Stop further execution
        }

        // Fetch current user asynchronously
        ((MainActivity) requireActivity()).getCurrentUserAsync(user -> {
            if (user != null) {
                currentUser = user;
                bindEventData(displayEvent);
                configureViewBasedOnRole(view);
            } else {
                Toast.makeText(requireContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Binds the event data to the UI elements.
     *
     * @param event The event object containing the data.
     */
    private void bindEventData(Event event) {
        binding.textviewEventName.setText(event.getEvent_name());
        binding.textviewEventDescription.setText(event.getEvent_description());

        // Display "Free" if event price is 0, otherwise display the price
        String eventPriceText = event.getEvent_price() == 0
                ? "Free"
                : String.valueOf(event.getEvent_price());
        binding.textviewEventPrice.setText(eventPriceText);

        // Display "No Entrant Limit" if event does not have a limit
        String maxEntrantsText = event.getEvent_max_entrants() == Integer.MAX_VALUE
                ? "No Entrant Limit"
                : String.valueOf(event.getEvent_max_entrants());
        binding.textviewMaxEntrants.setText(maxEntrantsText);

        binding.textviewGeolocationRequirement.setText(
                event.isGeolocation_requirement() ? "Geolocation required" : "No geolocation required"
        );

        binding.textviewGeolocationRequirement.setText(
                event.isGeolocation_requirement() ? "Geolocation required" : "No geolocation required"
        );

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String registrationDates = dateFormat.format(event.getRegistration_open()) +
                " - " + dateFormat.format(event.getRegistration_close());
        binding.textviewRegistrationDates.setText(registrationDates);

        // Load event image
        if (event.getPosterUri() != null && !event.getPosterUri().isEmpty()) {
            Glide.with(requireContext())
                    .load(Uri.parse(event.getPosterUri()))
                    .into(binding.imageviewEventImage);
        } else {
            binding.imageviewEventImage.setImageResource(R.drawable.example_event); // Placeholder image
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
        } else if ((previousDestinationId == R.id.EventListFragment || previousDestinationId == R.id.EditEventFragment) && currentUser.isOrganizer()) {
            setupOrganizerView(navController);
        } else {
            setupEntrantView();
        }
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
        binding.buttonJoinWaitingList.setOnClickListener(v -> onJoinWaitingListClick());
        binding.buttonLeaveWaitingList.setOnClickListener(v -> leaveWaitingList());
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

        // Set up the "View Map" button
        binding.buttonViewMap.setOnClickListener(v -> {
            waitingListController.getEntrantLocations(displayEvent.getEventId(), locations -> {
                LatLng[] latLngArray = locations.stream()
                        .map(location -> new LatLng(location[0], location[1]))
                        .toArray(LatLng[]::new);

                Bundle bundle = new Bundle();
                bundle.putParcelableArray("locations", latLngArray);
                navController.navigate(R.id.action_ViewEventFragment_to_MapFragment, bundle);
            }, e -> {
                Toast.makeText(requireContext(), "Failed to load entrant locations.", Toast.LENGTH_SHORT).show();
            });
        });

        binding.buttonEditEvent.setOnClickListener(v -> navigateToEditEvent(navController));

        setupBottomNavigationForOrganizer(navController);
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
        binding.buttonDeleteEventQrData.setOnClickListener(v -> deleteQRCode());

        hideUnusedButtonsForAdmin();
    }

    /**
     * Handles the user's click to join the waiting list button. If the event requires geolocation,
     * displays a warning dialog to confirm the user's consent before proceeding.
     */
    private void onJoinWaitingListClick() {
        // Check if geolocation is required for the event
        if (displayEvent.isGeolocation_requirement()) {
            // Show warning dialog
            showGeolocationWarningDialog(() -> {
                // Proceed to join the waitlist if user confirms
                getUserLocation(this::addUserToWaitingList);
            });
        } else {
            // Geolocation is not required; join waitlist directly
            addUserToWaitingList(null, null);
        }
    }

    /**
     * Displays a warning dialog if geolocation is required to join the waiting list.
     *
     * @param onConfirm Callback executed if the user confirms.
     */
    private void showGeolocationWarningDialog(Runnable onConfirm) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Geolocation Required")
                .setMessage("Joining the waiting list for this event requires sharing your location with the event organizer. Do you want to continue?")
                .setPositiveButton("Yes", (dialog, which) -> onConfirm.run())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Fetches the user's location if permissions are granted.
     * If permissions are denied, it requests permissions or shows an error message.
     */
    private void getUserLocation(OnLocationReceivedCallback callback) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationProvider.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                        } else {
                            Toast.makeText(requireContext(), "Location not available. Enable location services.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LocationError", "Error fetching location", e);
                        Toast.makeText(requireContext(), "Error fetching location. Try again.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "Location permission is required to join this event.", Toast.LENGTH_SHORT).show();
            requestLocationPermissions();
        }
    }

    /**
     * Requests location permissions from the user if not already granted.
     */
    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Adds the user to the waiting list with or without location data.
     */
    private void addUserToWaitingList(Double latitude, Double longitude) {
        String defaultStatus = "Waitlisted"; // Default status when joining the waiting list
        Log.d("ViewEventFragment", "Adding user to waiting list for eventId: " + displayEvent.getEventId());

        waitingListController.addUserToWaitingList(displayEvent.getEventId(), currentUser, currentUser.getDeviceId(), latitude, longitude, defaultStatus, new WaitingListController.OnActionCompleteListener() {
            @Override
            public void onSuccess() {
                Log.d("ViewEventFragment", "User added to waiting list successfully.");
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
     * Deleted the QR code data for the current event.
     */
    private void deleteQRCode() {
        controller.updateField(displayEvent, "qrCode", "");
        Toast.makeText(requireContext(), "Event QR code deleted", Toast.LENGTH_SHORT).show();
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
     * Callback interface for receiving location data.
     */
    private interface OnLocationReceivedCallback {
        void onLocationReceived(double latitude, double longitude);
    }
}
