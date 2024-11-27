package com.example.wizard_project.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wizard_project.Classes.Facility;
import com.example.wizard_project.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to display facility information in a list format for browsing.
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
        super(context, 0, facilities);
        this.context = context;
        this.facilityList = facilities;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.facility_card, parent, false);
        }

        // Get the Facility object for the current position in the list
        Facility currentFacility = facilityList.get(position);

        // Locate TextViews within the card layout for facility details
        TextView facilityName = convertView.findViewById(R.id.facility_card_name);
        TextView facilityLocation = convertView.findViewById(R.id.facility_card_location);

        // Bind facility name and location to their respective TextViews
        if (facilityName != null) {
            facilityName.setText(currentFacility.getFacility_name() != null ? currentFacility.getFacility_name() : "No Facility Name");
        }
        if (facilityLocation != null) {
            facilityLocation.setText(currentFacility.getFacility_location() != null ? currentFacility.getFacility_location() : "No Facility Location");
        }
        return convertView;
    }
}
