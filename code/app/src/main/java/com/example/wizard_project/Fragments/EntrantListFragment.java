package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Adapters.BrowseEntrantAdapter;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.EventController;
import com.example.wizard_project.databinding.FragmentEntrantListBinding;

import java.util.ArrayList;

/**
 * EntrantListFragment represents a view of the list of entrants for an event.
 */
public class EntrantListFragment extends Fragment {
    private FragmentEntrantListBinding binding;
    private EventController eventController;
    private ArrayList<User> entrantList = new ArrayList<User>();
    private BrowseEntrantAdapter adapter;

    /**
     * Creates a new instance of EntrantListFragment with event information passed.
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntrantListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);
        eventController = new EventController();
        ListView entrantListView = binding.entrantListview;
        adapter = new BrowseEntrantAdapter(getContext(), entrantList);
        entrantListView.setAdapter(adapter);

        // Get the event object.
        assert getArguments() != null;
        Event event = (Event) getArguments().getSerializable("event");

        // Add the users to the list to be displayed.
        assert event != null;
        eventController.getWaitlistEntrants(event, new EventController.waitListCallback() {
            @Override
            public void onCallback(ArrayList<User> users) {
                entrantList.clear();
                entrantList.addAll(users);
                adapter.notifyDataSetChanged();
            }
        });


    }
}
