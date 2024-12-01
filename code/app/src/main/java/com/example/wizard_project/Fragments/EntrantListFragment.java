package com.example.wizard_project.Fragments;

import static android.icu.number.NumberRangeFormatter.with;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.app.NotificationManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.wizard_project.Adapters.BrowseEntrantAdapter;
import com.example.wizard_project.Classes.Entrant;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Controllers.EventController;
import com.example.wizard_project.LotterySystem;
import com.example.wizard_project.Manifest;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEntrantListBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * EntrantListFragment represents a view of the list of entrants for an event.
 */
public class EntrantListFragment extends Fragment implements SampleAttendeeDialog.SampleAttendeesListener {
    private FragmentEntrantListBinding binding;
    private EventController eventController;
    private BrowseEntrantAdapter adapter;
    private Event event;

    /**
     * Creates a new instance of EntrantListFragment with event information passed.
     *
     * @param event The event with the information to be passed in a serialized format.
     * @return A new instance of EntrantListFragment with event information passed.
     */
    public static EntrantListFragment newInstance(Event event) {
        Bundle args = new Bundle();
        args.putSerializable("event", event);

        EntrantListFragment fragment = new EntrantListFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void setDrawAmount(int drawAmount) {
        eventController.setDrawCount(event, drawAmount);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntrantListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the event controller
        eventController = new EventController();

        setupUIComponents();
        fetchAndDisplayEntrants();
    }

    /**
     * Sets up UI components like buttons and list adapters.
     */
    private void setupUIComponents() {
        ListView entrantListView = binding.entrantListview;

        // Set up filter button
        Button filterButton = binding.filterButton;
        filterButton.setOnClickListener(this::showFilterMenu);

        // Set up sample attendees button
        Button sampleAttendeesButton = binding.sampleAttendeesButton;
        sampleAttendeesButton.setOnClickListener(view -> {
            if (adapter != null) {
                SampleAttendeeDialog dialog = SampleAttendeeDialog.newInstance(adapter.getCount());
                dialog.show(getChildFragmentManager(), "SampleAttendeesDialog");
            }
        });

        // Set up cancel entrant button
        Button cancelEntrantButton = binding.cancelEntrantButton;
        cancelEntrantButton.setOnClickListener(view -> {
            SparseBooleanArray checkedEntrants = entrantListView.getCheckedItemPositions();
            for (int i = 0; i < checkedEntrants.size(); i++) {
                int itemKey = checkedEntrants.keyAt(i);

                if (checkedEntrants.get(itemKey)) {
                    Entrant currentEntrant = adapter.getItem(itemKey);

                    if (currentEntrant.getStatus().equals("Selected")) {
                        currentEntrant.setStatus("Cancelled");
                        eventController.updateEntrantStatus(event, currentEntrant);
                        Toast.makeText(requireContext(), "Successfully cancelled entrant: " + currentEntrant.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            adapter.notifyDataSetChanged();
        });

        //setup sending notification button
        Button makeNotif = binding.sendNotificationButton;
        makeNotif.setOnClickListener(view -> {
            com.example.wizard_project.Classes.Notification.makeNotification();
        });


        // Set up re-draw attendees button
        Button redrawAttendeesButton = binding.redrawAttendeesButton;
        redrawAttendeesButton.setOnClickListener(view -> {
            ArrayList<Entrant> cancelledEntrants = new ArrayList<>();
            ArrayList<Entrant> notSelectedEntrants = new ArrayList<>();
            int selectedCount = 0;
            // Get a list of cancelled and non-selected entrants.
            for(int i = 0; i < adapter.getCount(); i++) {
                Entrant entrant = adapter.getItem(i);

                switch (entrant.getStatus()) {
                    case "Cancelled":
                        cancelledEntrants.add(entrant);
                        break;
                    case "Not Selected":
                        notSelectedEntrants.add(entrant);
                        break;
                    case "Selected":
                    case "Enrolled":
                        selectedCount++;
                        break;
                }
            }

            // Only re-draw if there are cancelled entrants and there are replacements available.
            if (cancelledEntrants.isEmpty()) {
                Toast.makeText(requireContext(), "No replacements must be redrawn.", Toast.LENGTH_SHORT).show();
            }
            else if (notSelectedEntrants.isEmpty()) {
                Toast.makeText(requireContext(), "No replacements are available.", Toast.LENGTH_SHORT).show(); // Edge case: Message shows when no entrants need to be redrawn, but there are cancelled entrants.
            }
            else {
                int finalSelectedCount = selectedCount;
                eventController.getDrawCount(event, new EventController.drawCountCallback() {
                    @Override
                    public void onSuccess(int drawCount) {
                        int redrawCount = drawCount - finalSelectedCount;
                        if (redrawCount == 0) {
                            Toast.makeText(requireContext(), "No replacements must be redrawn.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            LotterySystem lotterySystem = new LotterySystem();
                            lotterySystem.drawEntrants(event, notSelectedEntrants, redrawCount);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(requireContext(), redrawCount + " entrants have been redrawn.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("DrawCountRetrievalError", "Error retrieving draw count: ", e);
                    }
                });
            }
        });
    }

    /**
     * Fetches the list of entrants from the waiting list for the specified event and updates the UI.
     */
    private void fetchAndDisplayEntrants() {
        // Retrieve event from arguments
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        if (event == null || event.getEventId() == null) {
            Log.e("EntrantListFragment", "Event or Event ID is null. Cannot fetch entrants.");
            Toast.makeText(requireContext(), "Invalid event data.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch waiting list data
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

                // Set up adapter and update list
                adapter = new BrowseEntrantAdapter(requireContext(), entrants);
                binding.entrantListview.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EntrantListFragment", "Error fetching waiting list", e);
            }
        });
    }

    /**
     * Displays a filter menu to filter entrants by their status.
     *
     * @param view The view triggering the filter menu.
     */
    private void showFilterMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.entrant_filter_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                popupMenu.getMenu().getItem(i).setChecked(false);
            }
            menuItem.setChecked(true);

            String filterStatus = Objects.requireNonNull(menuItem.getTitle()).toString();
            if (adapter != null) {
                adapter.getFilter().filter(filterStatus);
            }
            return true;
        });
        popupMenu.show();
    }


}
