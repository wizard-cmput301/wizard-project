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
import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom adapter for displaying a list of facilities in a ListView.
 * Each facility is represented by a card containing its details.
 */
public class BrowseFacilityAdapter extends ArrayAdapter<Facility> {
    private final List<Facility> facilityList;
    private final Context context;

    /**
     * Constructor to initialize the adapter with facility data.
     *
     * @param context    The context in which the adapter is being used.
     * @param facilities The list of facilities to be displayed.
     */
    public BrowseFacilityAdapter(Context context, ArrayList<Facility> facilities) {
        super(context, R.layout.facility_card, facilities);
        this.context = context;
        this.facilityList = facilities;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        // Reuse or inflate the facility card layout
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.facility_card, parent, false);
            holder = new ViewHolder();
            holder.facilityImage = convertView.findViewById(R.id.facility_card_image);
            holder.facilityName = convertView.findViewById(R.id.facility_card_name);
            holder.facilityLocation = convertView.findViewById(R.id.facility_card_location);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the facility at the current position
        Facility facility = facilityList.get(position);

        // Set the facility image
        if (facility.getposterUri() != null && !facility.getposterUri().isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(facility.getposterUri()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.example_event)
                    .error(R.drawable.example_event)
                    .into(holder.facilityImage);
        } else {
            holder.facilityImage.setImageResource(R.drawable.example_event);
        }

        // Set facility name and location
        holder.facilityName.setText(facility.getFacility_name());
        holder.facilityLocation.setText(facility.getFacility_location());

        return convertView;
    }

    /**
     * ViewHolder pattern for efficient ListView rendering.
     */
    private static class ViewHolder {
        ImageView facilityImage;
        TextView facilityName;
        TextView facilityLocation;
    }
}
