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

import java.text.SimpleDateFormat;
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
    private String eventLocation = "";
    int newPrice = 0;



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

        // Get any passed Event object through the arguments.
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
            assert event != null;

            // Get the facilityId
            facilityId = event.getFacilityId();

            // Pre-fill the EditText fields if editing an existing event.
            eventName.setText(String.format("%s", event.getEvent_name()));
            eventPrice.setText(String.format("%d", event.getEvent_price()));
            eventWaitlist.setText(String.format("%d", event.getEvent_waitlist_limit()));
            selectedDeadlineDate = event.getEvent_deadline();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDeadlineDate);
            eventDeadline.setText(String.format("%02d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH)));
        }
        else {
            // If no Event object was passed, get the facility details based on the user ID.
            event = null;
            facilityController.getFacility(userId, new FacilityController.facilityCallback() {
                @Override
                public void onCallback(Facility facility) {
                    facilityId = facility.getFacilityId();
                    eventLocation = facility.getFacility_name();
                }
            });
        }

        // Submit the fields for the event attributes.
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = eventName.getText().toString();
                Date newDeadline = selectedDeadlineDate;


                if(!eventWaitlist.getText().toString().isEmpty()) {
                    newWaitlistLimit = Integer.valueOf(eventWaitlist.getText().toString());
                }
                else {
                    // If no limit is entered, then any amount of users can join the waitlist.
                    newWaitlistLimit = Integer.MAX_VALUE;
                }

                if (eventPrice.getText().toString().trim().isEmpty()) {
                    eventPrice.setError("Please enter a valid price.");
                }
                else {
                    newPrice = Integer.parseInt(eventPrice.getText().toString());
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
                    // If editing an event, update its attributes.
                    if (event != null) {
                        event.setEvent_name(newName);
                        event.setEvent_price(newPrice);
                        event.setEvent_waitlist(newWaitlistLimit);
                        event.setEvent_deadline(newDeadline);
                        eventController.updateEvent(event);

                        // Pass the updated Event object to the ViewEventFragment and navigate.
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event", event);
                        navController.navigate(R.id.action_EditEventFragment_to_ViewEventFragment, bundle);
                    } else {
                        // Create a new Event object.
                        Event newEvent = eventController.createEvent(facilityId, newName, newPrice, newWaitlistLimit, newDeadline, eventLocation);
                        newEvent.setEvent_location(eventLocation);

                        // Pass the new Event object to the ViewEventFragment and navigate.
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event", newEvent);
                        navController.navigate(R.id.action_EditEventFragment_to_ViewEventFragment, bundle);
                    }
                }
            }
        });

        // Button to upload a promotional poster for the event.
        editPosterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Date selector button for the event deadline.
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        eventDeadline.setText(String.format("%02d-%02d-%02d", year, month+1, day));

                        calendar.set(year, month, day, 23, 59, 59);
                        selectedDeadlineDate = calendar.getTime();
                    }
                }, currentYear, currentMonth, currentDay);
                dialog.show();
            }
        });
    }
}
