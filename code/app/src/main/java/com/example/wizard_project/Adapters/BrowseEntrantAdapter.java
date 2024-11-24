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

import com.example.wizard_project.Classes.User;
import com.example.wizard_project.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * BrowseEntrantAdapter is a custom ArrayAdapter to display the list of entrants for an event.
 */
public class BrowseEntrantAdapter extends ArrayAdapter<User> implements Filterable {

    private List<User> userList;
    private List<User> filterList;
    private Context context;

    /**
     * Constructs a new BrowseEntrantAdapter with a list of entrants and the context data.
     * @param context The context used for layout inflation.
     * @param users The list of users to be displayed.
     */
    public BrowseEntrantAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
        this.context = context;
        this.userList = users;
        this.filterList = new ArrayList<>(users);
        Log.d("filterSize", String.valueOf(filterList.size()));
    }

    @Override
    public int getCount() {
        return filterList.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.entrant_list_content, parent, false);
        }

        ShapeableImageView userProfilePicture = convertView.findViewById(R.id.entrant_profile_image);
        TextView userName = convertView.findViewById(R.id.entrant_user_name);
        TextView userStatus = convertView.findViewById(R.id.entrant_status);
        CheckBox selectionBox = convertView.findViewById(R.id.entrant_checkbox);
        User currentUser = filterList.get(position);

        userName.setText(currentUser.getName());
        userStatus.setText(currentUser.getStatus());
        selectionBox.setText("");

        return convertView;
    }


    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterStatus;
                FilterResults filterResults = new FilterResults();
                List<User> filteredUsers = new ArrayList<User>();

                if (constraint == null) {
                    filterStatus = "";
                }
                else {
                    filterStatus = constraint.toString();
                    Log.d("ConstraintFilter", constraint.toString());
                }

                if (filterStatus.isEmpty() || filterStatus.equals("All")) {
                    filteredUsers.addAll(userList);
                }
                else {
                    for (User user: userList) {
                        Log.d("userStatus", user.getStatus());
                        if (filterStatus.equals(user.getStatus())) {
                            Log.d("UserAdd", user.getName());
                            filteredUsers.add(user);
                        }
                    }
                }

                filterResults.values = filteredUsers;
                filterResults.count = filteredUsers.size();
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filterList.clear();
                filterList = (List<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}