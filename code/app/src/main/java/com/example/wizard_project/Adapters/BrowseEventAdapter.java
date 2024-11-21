package com.example.wizard_project.Adapters;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Controllers.FacilityController;
import com.example.wizard_project.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Adapter to display event information in a list format for browsing.
 */
/**
 * BrowseEventAdapter is a custom ArrayAdapter to display a list of events for a given facility.
 */
public class BrowseEventAdapter extends ArrayAdapter<Event> {
    private List<Event> eventList;
    private Context context;

    /**
     * Constructor to initialize the adapter with event data.
     *
     * @param context The context in which the adapter is being used.
     * @param events  The list of events to be displayed.
     */
    /**
     * Construct a new BrowseEventAdapter with a list of events and the context data.
     * @param context The context used for layout inflation.
     * @param events The list of events to be displayed.
     */
    public BrowseEventAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.eventList = events;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.event_card, parent, false);
        }

        // Get the Event object for the current position in the list
        Event currentEvent = eventList.get(position);

        // Locate TextViews within the card layout for event details
        TextView eventTitle = convertView.findViewById(R.id.event_card_title);
        TextView eventDetails = convertView.findViewById(R.id.event_card_details);
        TextView eventLocation = convertView.findViewById(R.id.event_card_location);
        TextView eventAvailability = convertView.findViewById(R.id.event_card_availability);
        TextView eventDeadline = convertView.findViewById(R.id.event_card_deadline);
        Date currentTime = new Date();

        // Bind event data to each view
        if (eventTitle != null) {
            eventTitle.setText(currentEvent.getEvent_name() != null ? currentEvent.getEvent_name() : "No Title");
        }

        if (eventDetails != null) {
            eventDetails.setText(String.format("$%d per lesson", currentEvent.getEvent_price()));
        }

        if (eventLocation != null) {
            String location = currentEvent.getEvent_location() != null
                    ? currentEvent.getEvent_location()
                    : "No Location";
            eventLocation.setText(location);
        }

        if (eventAvailability != null) {
            int slotsAvailable = currentEvent.getRemainingWaitlistLimit();
            eventAvailability.setText(String.format("%d Slots Available", slotsAvailable));
        }

        if (eventDeadline != null) {
            String deadlineText = "No Deadline";
            if (currentEvent.getEvent_deadline() != null) {
                long daysRemaining = (currentEvent.getEvent_deadline().getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
                deadlineText = String.format("Closes In: %d Days", daysRemaining);
            }
            eventDeadline.setText(deadlineText);
        }

        return convertView;
    }
}