package com.example.wizard_project.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wizard_project.Classes.Entrant;
import com.example.wizard_project.R;

import java.util.ArrayList;
import java.util.List;

/**
 * BrowseEntrantAdapter is a custom ArrayAdapter to display the list of entrants for an event.
 */
public class BrowseEntrantAdapter extends ArrayAdapter<Entrant> implements Filterable {
    private final List<Entrant> originalList; // Full list of entrants
    private final List<Entrant> filteredList; // Filtered list of entrants
    private final Context context;

    /**
     * Constructs a new BrowseEntrantAdapter with a list of entrants and the application context.
     *
     * @param context  The context used for layout inflation.
     * @param entrants The list of entrants to display.
     */
    public BrowseEntrantAdapter(Context context, ArrayList<Entrant> entrants) {
        super(context, 0, entrants);
        this.context = context;
        this.originalList = entrants;
        this.filteredList = new ArrayList<>(entrants); // Initialize filteredList with a copy of originalList
    }

    /**
     * Returns the number of filtered items.
     *
     * @return The size of the filtered entrant list.
     */
    @Override
    public int getCount() {
        return filteredList.size();
    }

    /**
     * Returns the entrant at the specified position in the filtered list.
     *
     * @param position The position of the item in the list.
     * @return The entrant at the specified position.
     */
    @Override
    public Entrant getItem(int position) {
        return filteredList.get(position);
    }


    /**
     * Returns a view for each item in the filtered list.
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent view that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.entrant_list_content, parent, false);
        }

        // Get the views from the layout
        ImageView profilePicture = convertView.findViewById(R.id.entrant_profile_image); // TODO: Set to the user's profile picture.
        TextView userName = convertView.findViewById(R.id.entrant_user_name);
        TextView userStatus = convertView.findViewById(R.id.entrant_status);
        CheckBox checkbox = convertView.findViewById(R.id.entrant_checkbox);

        // Fill the views with data from the entrant
        Entrant entrant = getItem(position);
        userName.setText(entrant.getName());
        userStatus.setText(entrant.getStatus());


        // Set the on-click listener for each checkbox.
        ListView listView = (ListView) parent;
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                listView.setItemChecked(position, b);
            }
        });

        return convertView;
    }

    /**
     * Returns a filter for filtering entrants by their status.
     *
     * @return A filter object that performs filtering on the entrant list.
     */
    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Entrant> filtered = new ArrayList<>();

                // If no constraint is provided, show all entrants
                if (constraint == null || constraint.toString().isEmpty() || constraint.toString().equals("All")) {
                    filtered.addAll(originalList);

                    // Otherwise, filter entrants based on the selected status
                } else {
                    for (Entrant entrant : originalList) {
                        if (entrant.getStatus().equalsIgnoreCase(constraint.toString())) {
                            filtered.add(entrant);
                        }
                    }
                }

                results.values = filtered;
                results.count = filtered.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // Update the filtered list and notify the adapter
                filteredList.clear();
                filteredList.addAll((List<Entrant>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}
