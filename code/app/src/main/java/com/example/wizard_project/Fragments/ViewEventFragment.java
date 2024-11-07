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

import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.databinding.FragmentViewEventBinding;


public class ViewEventFragment extends Fragment {
    private FragmentViewEventBinding binding;

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

        assert getArguments() != null;
        Event event = (Event) getArguments().getSerializable("event");

        eventName.setText(String.format("Event Name: %s", event.getEvent_name()));
        eventPrice.setText(String.format("Price: %d", event.getEvent_price()));
        eventWaitlist.setText(String.format("Availability: %d Spots", event.getEvent_waitlist()));
        eventDeadline.setText(String.format("Deadline: %d Days", event.getEvent_deadline()));

        editEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditEventFragment editFragment = EditEventFragment.newInstance(event);

            }
        });


    }
}
