package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentViewEventBinding;

import java.util.Date;

/**
 * ViewEventFragment allows the organizer to view their event, providing multiple functionalities to manage the event.
 */
public class ViewEventFragment extends Fragment {
    private FragmentViewEventBinding binding;

    /**
     * Creates a new instance of ViewEventFragment with event information passed.
     * @param event The event with the information to be passed in a serialized format.
     * @return A new instance of ViewEventFragment with event information passed.
     */
    public static ViewEventFragment newInstance(Event event) {
        Bundle args = new Bundle();
        args.putSerializable("event", event);

        ViewEventFragment fragment = new ViewEventFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);
        TextView eventName = binding.eventViewEventName;
        TextView eventPrice = binding.eventViewPrice;
        TextView eventWaitlist = binding.eventViewWaitlist;
        TextView eventDeadline = binding.eventViewDeadline;
        Button editPosterButton = binding.eventViewPosterButton;
        Button viewFacilityButton = binding.viewFacilityButton;
        Button editEventButton = binding.editEventButton;
        Button viewEntrantsButton = binding.viewEntrantsButton;
        Button mapViewButton = binding.mapViewButton;
        Button viewQRButton = binding.viewQrButton;
        Date currentTime = new Date();


        assert getArguments() != null;
        Event event = (Event) getArguments().getSerializable("event");
        assert event != null;
        long deadlineTimeRemaining = event.getEvent_deadline().getTime() - currentTime.getTime();

        int daysRemaining = Math.round((float) deadlineTimeRemaining / (1000 * 60 * 60 * 24));

        eventName.setText(String.format("Event Name: %s", event.getEvent_name()));
        eventPrice.setText(String.format("Price: %d", event.getEvent_price()));
        eventWaitlist.setText(String.format("Availability: %d Spots", event.getEvent_waitlist_limit()));
        eventDeadline.setText(String.format("Deadline: %d Days", daysRemaining));

        editEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("event", event);
                navController.navigate(R.id.action_ViewEventFragment_to_EditEventFragment, bundle);
            }
        });

        viewEntrantsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }
}
