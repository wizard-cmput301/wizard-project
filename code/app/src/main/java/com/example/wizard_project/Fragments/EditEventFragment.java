package com.example.wizard_project.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.service.controls.Control;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.EventController;
import com.example.wizard_project.Controllers.FacilityController;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentEditEventBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * EditEventFragment allows the user to create or edit their event.
 */
public class EditEventFragment extends Fragment {
    private FragmentEditEventBinding binding;
    Date selectedDeadlineDate;
    int newWaitlistLimit = 0;
    private String facilityId;
    private EventController eventController;
    private FacilityController facilityController;
    private User currentUser;



    /**
     * Creates a new instance of EditEventFragment with event information passed.
     * @param event The event with the information to be passed in a serialized format.
     * @return A new instance of EditEventFragment containing the event as an argument.
     */
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

        MainActivity mainActivity = (MainActivity) requireActivity();
        NavController navController = NavHostFragment.findNavController(this);
        currentUser = mainActivity.getCurrentUser();
        String userId = currentUser.getDeviceId();
        facilityController = new FacilityController();
        eventController = new EventController();
        EditText eventName = binding.eventNameEdittext;
        EditText eventPrice = binding.eventPriceEdittext;
        EditText eventWaitlist = binding.eventWaitlistEdittext;
        TextView eventDeadline = binding.eventDeadlineText;
        Button editPosterButton = binding.eventEditPosterButton;
        Button doneButton = binding.eventDoneButton;
        Button selectDateButton = binding.datePickerButton;
        ImageView eventPoster = binding.eventEditImageview;
        Date currentTime = new Date();
        Event event;

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
            assert event != null;
            long deadlineTimeRemaining = event.getEvent_deadline().getTime() - currentTime.getTime();
            int daysRemaining = Math.round((float) deadlineTimeRemaining / (1000 * 60 * 60 * 24));
            facilityId = event.getFacilityId();

            eventName.setText(String.format("Event Name: %s", event.getEvent_name()));
            eventPrice.setText(String.format("Price: %d", event.getEvent_price()));
            eventWaitlist.setText(String.format("Availability: %d Spots", event.getRemainingWaitlistLimit()));
        }
        else {
            event = null;
            facilityController.getFacility(userId, new FacilityController.facilityCallback() {
                @Override
                public void onCallback(Facility facility) {
                    facilityId = facility.getFacilityId();
                }
            });
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = eventName.getText().toString();
                int newPrice = Integer.parseInt(eventPrice.getText().toString());
                Date newDeadline = selectedDeadlineDate;

                if (!eventPrice.getText().toString().isEmpty()) {
                    newWaitlistLimit = Integer.parseInt(eventWaitlist.getText().toString());
                }

                if (newName.trim().isEmpty()) {
                    eventName.setError("Please enter a valid event name.");
                }
                else if (newWaitlistLimit < 1) {
                    eventWaitlist.setError("Please enter a valid waitlist limit.");
                }
                else if (selectedDeadlineDate == null) {
                    eventDeadline.setError("Please enter a valid date.");
                }
                else {
                    if (event != null) {
                        event.setEvent_name(newName);
                        event.setEvent_price(newPrice);
                        event.setEvent_waitlist(newWaitlistLimit);
                        event.setEvent_deadline(newDeadline);
                        eventController.updateEvent(event);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event", event);
                        navController.navigate(R.id.action_EditEventFragment_to_ViewEventFragment, bundle);
                    } else {
                        Event newEvent = eventController.createEvent(facilityId, newName, newPrice, newWaitlistLimit, newDeadline);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event", newEvent);
                        navController.navigate(R.id.action_EditEventFragment_to_ViewEventFragment, bundle);
                    }
                }
            }
        });

        editPosterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        eventDeadline.setText(String.format("%d-%d-%d", year, month, day));

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day);
                        selectedDeadlineDate = calendar.getTime();
                    }
                }, 2024, 10, 30);
                dialog.show();
            }
        });
    }
}
