package com.example.wizard_project.Adapters;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class BrowseEventAdapter extends ArrayAdapter<Event> {
    private List<Event> eventList;
    private Context context;

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

        Event currentEvent = eventList.get(position);

        ShapeableImageView eventImage = convertView.findViewById(R.id.event_card_image);
        TextView eventTitle = convertView.findViewById(R.id.event_card_title);
        TextView eventDetails = convertView.findViewById(R.id.event_card_details);
        TextView eventLocation = convertView.findViewById(R.id.event_card_location);
        TextView eventAvailability = convertView.findViewById(R.id.event_card_availability);
        TextView eventDeadline = convertView.findViewById(R.id.event_card_deadline);

        eventTitle.setText(currentEvent.getEvent_name());
        eventDetails.setText(String.format("$%d", currentEvent.getEvent_price()));
        eventLocation.setText(currentEvent.getEvent_location().getFacility_location());
        eventAvailability.setText(String.format("$d Spots Available", currentEvent.getEvent_waitlist()));
        eventDeadline.setText(String.format("%d Days Remaining", currentEvent.getEvent_deadline()));

        return convertView;
    }
}
