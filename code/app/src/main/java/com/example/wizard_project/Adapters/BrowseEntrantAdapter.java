package com.example.wizard_project.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wizard_project.Classes.Entrant;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.Controllers.WaitingListController;
import com.example.wizard_project.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BrowseEntrantAdapter is a custom ArrayAdapter to display the list of entrants for an event.
 */
public class BrowseEntrantAdapter extends ArrayAdapter<Entrant> implements Filterable {
    private List<Entrant> originalList;
    private List<Entrant> filteredList;
    private Context context;


    public BrowseEntrantAdapter(Context context, ArrayList<Entrant> entrants) {
        super(context, 0, entrants);
        this.context = context;
        this.originalList = entrants;
        this.filteredList = new ArrayList<>(entrants);
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Entrant getItem(int position) {
        return filteredList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.entrant_list_content, parent, false);
        }

        TextView userName = convertView.findViewById(R.id.entrant_user_name);
        TextView userStatus = convertView.findViewById(R.id.entrant_status);

        Entrant entrant = getItem(position);
        userName.setText(entrant.getName());
        userStatus.setText(entrant.getStatus());

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Entrant> filtered = new ArrayList<>();

                if (constraint == null || constraint.toString().isEmpty() || constraint.toString().equals("All")) {
                    filtered.addAll(originalList);
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
                filteredList.clear();
                filteredList.addAll((List<Entrant>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}
