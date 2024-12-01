package com.example.wizard_project.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Custom adapter for displaying a list of events in a ListView.
 * Each event is represented by a card containing its details.
 */
public class BrowseEventAdapter extends ArrayAdapter<Event> {
    private final Context context;
    private final ArrayList<Event> events;

    /**
     * Constructor to initialize the adapter with the context and list of events.
     *
     * @param context The context used for layout inflation.
     * @param events  The list of events to be displayed.
     */
    public BrowseEventAdapter(Context context, ArrayList<Event> events) {
        super(context, R.layout.event_card, events);
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        // Reuse or inflate the event card layout
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.event_card, parent, false);
            holder = new ViewHolder();
            holder.eventImage = convertView.findViewById(R.id.event_card_image);
            holder.eventTitle = convertView.findViewById(R.id.event_card_title);
            holder.eventDescription = convertView.findViewById(R.id.event_card_description);
            holder.eventLocation = convertView.findViewById(R.id.event_card_location);
            holder.eventMaxEntrants = convertView.findViewById(R.id.event_card_max_entrants);
            holder.eventRegistration = convertView.findViewById(R.id.event_card_registration);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the event at the current position
        Event event = events.get(position);

        // Set the event image
        if (event.getPosterUri() != null && !event.getPosterUri().isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(event.getPosterUri()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.example_event)
                    .error(R.drawable.example_event)
                    .into(holder.eventImage);
        } else {
            holder.eventImage.setImageResource(R.drawable.example_event);
        }

        // Set event name and description
        holder.eventTitle.setText(event.getEvent_name());
        holder.eventDescription.setText(event.getEvent_description());

        // Set event location
        String location = event.getEvent_location() != null ? event.getEvent_location() : "Unknown Location";
        holder.eventLocation.setText("Location: " + location);

        // Set event max entrants
        if (event.getEvent_max_entrants() == Integer.MAX_VALUE) {
            holder.eventMaxEntrants.setText("No Entrant Limit");
        } else {
            holder.eventMaxEntrants.setText("Max Entrants: " + event.getEvent_max_entrants());
        }

        // Set event registration dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String registrationOpen = event.getRegistration_open() != null
                ? dateFormat.format(event.getRegistration_open())
                : "N/A";
        String registrationClose = event.getRegistration_close() != null
                ? dateFormat.format(event.getRegistration_close())
                : "N/A";
        holder.eventRegistration.setText("Registration: " + registrationOpen + " - " + registrationClose);

        return convertView;
    }

    /**
     * ViewHolder pattern for efficient ListView rendering.
     */
    private static class ViewHolder {
        ImageView eventImage;
        TextView eventTitle;
        TextView eventDescription;
        TextView eventLocation;
        TextView eventMaxEntrants;
        TextView eventRegistration;
    }
}
