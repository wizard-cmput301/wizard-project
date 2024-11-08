package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.databinding.FragmentEditEventBinding;

public class EditEventFragment extends Fragment {
    private FragmentEditEventBinding binding;

    public static EditEventFragment newInstance(Event event) {
        Bundle args = new Bundle();
        args.putSerializable("event", event);

        EditEventFragment fragment = new EditEventFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText eventName = binding.eventNameEdittext;
        EditText eventPrice = binding.eventPriceEdittext;
        EditText eventWaitlist = binding.eventWaitlistEdittext;
        EditText eventDeadline = binding.eventDeadlineEdittext;
        Button editPosterButton = binding.eventEditPosterButton;
        Button doneButton = binding.eventDoneButton;
        ImageView eventPoster = binding.eventEditImageview;
        Event event;



        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");

            eventName.setText(String.format("Event Name: %s", event.getEvent_name()));
            eventPrice.setText(String.format("Price: %d", event.getEvent_price()));
            eventWaitlist.setText(String.format("Availability: %d Spots", event.getEvent_waitlist()));
            eventDeadline.setText(String.format("Deadline: %d Days", event.getEvent_deadline()));
        }
        else { event = null; }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = eventName.getText().toString();
                int newPrice = Integer.parseInt(eventPrice.getText().toString());
                int newWaitlist = Integer.parseInt(eventWaitlist.getText().toString());
                int newDeadline = Integer.parseInt(eventDeadline.getText().toString());

                if (event != null) {
                    event.setEvent_name(newName);
                    event.setEvent_price(newPrice);
                    event.setEvent_waitlist(newWaitlist);
                    event.setEvent_deadline(newDeadline);
                }

                else {
                    Event newEvent = new Event(newName, newPrice, newWaitlist, newDeadline);

                }
            }
        });

        editPosterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
