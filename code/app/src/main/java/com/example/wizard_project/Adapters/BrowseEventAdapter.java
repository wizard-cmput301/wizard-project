package com.example.wizard_project.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * BrowseEventAdapter is a custom ArrayAdapter to display a list of events for a given facility.
 * It inflates a custom layout for each event and populates it with event details.
 */
public class BrowseEventAdapter extends ArrayAdapter<Event> {
    private final List<Event> eventList;
    private final Context context;

    /**
     * Construct a new BrowseEventAdapter with a list of events and the context data.
     *
     * @param context The context used for layout inflation.
     * @param events  The list of events to be displayed.
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

        // Get the current event for this position
        Event currentEvent = eventList.get(position);

        // Locate views in the event card layout
        TextView eventTitle = convertView.findViewById(R.id.event_card_title);
        TextView eventDescription = convertView.findViewById(R.id.event_card_description);
        TextView eventLocation = convertView.findViewById(R.id.event_card_location);
        TextView eventAvailability = convertView.findViewById(R.id.event_card_availability);
        TextView eventDeadline = convertView.findViewById(R.id.event_card_deadline);
        ShapeableImageView eventImage = convertView.findViewById(R.id.event_card_image);

        // Bind event data to each view
        if (eventTitle != null) {
            eventTitle.setText(currentEvent.getEvent_name() != null ? currentEvent.getEvent_name() : "No Title");
        }

        if (eventDescription != null) {
            eventDescription.setText(currentEvent.getEvent_description() != null ? currentEvent.getEvent_description() : "No Description");
        }

        if (eventLocation != null) {
            String location = currentEvent.getEvent_location() != null
                    ? currentEvent.getEvent_location()
                    : "No Location";
            eventLocation.setText(location);
        }

        if (eventAvailability != null) {
            int maxEntrants = currentEvent.getEvent_max_entrants();
            eventAvailability.setText(String.format("%d Entrants Available", maxEntrants));
        }

        if (eventDeadline != null) {
            String deadlineText = "No Deadline";
            if (currentEvent.getRegistration_close() != null) {
                long daysRemaining = (currentEvent.getRegistration_close().getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
                if (daysRemaining > 0) {
                    deadlineText = String.format("Closes In: %d Days", daysRemaining);
                } else {
                    deadlineText = "Registration Closed";
                }
            }
            eventDeadline.setText(deadlineText);
        }

        if (eventImage != null) {
            String imagePath = currentEvent.getEvent_image_path();
            if (imagePath != null && !imagePath.isEmpty()) {
                // Load the image using Glide
                Glide.with(context).load(imagePath).into(eventImage);
            } else {
                eventImage.setImageResource(R.drawable.example_event); // Placeholder image
            }
        }
        return convertView;
    }
}
