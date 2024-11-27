package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Adapters.BrowseEntrantAdapter;
import com.example.wizard_project.Classes.Entrant;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.EventController;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEntrantListBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * EntrantListFragment represents a view of the list of entrants for an event.
 */
public class EntrantListFragment extends Fragment implements SampleAttendeeDialog.SampleAttendeesListener {
    private FragmentEntrantListBinding binding;
    private EventController eventController;
    private ArrayList<User> entrantList = new ArrayList<User>();
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

        ListView entrantListView = binding.entrantListview;

        NavController navController = NavHostFragment.findNavController(this);
        eventController = new EventController();

        // Get the event object.
        assert getArguments() != null;
        event = (Event) getArguments().getSerializable("event");

        // Get the entrant list from the event
        assert event != null;
        eventController.getWaitingList(event.getEventId(), new EventController.WaitingListCallback() {
            @Override
            public void onSuccess(ArrayList<Map<String, String>> waitingList) {
                ArrayList<Entrant> entrants = new ArrayList<>();

                for (Map<String, String> entry : waitingList) {
                    String name = entry.get("name");
                    String status = entry.get("status");
                    String userId = entry.get("userId");
                    Double latitude = entry.containsKey("latitude") ? Double.valueOf(entry.get("latitude")) : null;
                    Double longitude = entry.containsKey("longitude") ? Double.valueOf(entry.get("longitude")) : null;

                    entrants.add(new Entrant(name, status, userId, latitude, longitude));
                }

                adapter = new BrowseEntrantAdapter(getContext(), entrants);
                entrantListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EntrantListFragment", "Error fetching waiting list", e);
            }
        });

        Button sampleAttendeesButton = binding.sampleAttendeesButton;
        Button filterButton = binding.filterButton;

        sampleAttendeesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SampleAttendeeDialog dialog = SampleAttendeeDialog.newInstance(entrantList.size());
                dialog.show(getChildFragmentManager(), "SampleAttendeesDialog");
            }
        });

        filterButton.setOnClickListener(filterView -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), filterView);
            popupMenu.getMenuInflater().inflate(R.menu.entrant_filter_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                    if (!(popupMenu.getMenu().getItem(i) == menuItem)) {
                        popupMenu.getMenu().getItem(i).setChecked(false);
                    }
                }

                menuItem.setChecked(true);

                String filterStatus = Objects.requireNonNull(menuItem.getTitle()).toString();
                adapter.getFilter().filter(filterStatus);
                return true;
            });
            popupMenu.show();
        });
    }
}
