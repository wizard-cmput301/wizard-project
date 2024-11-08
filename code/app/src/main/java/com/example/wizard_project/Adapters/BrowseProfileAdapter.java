package com.example.wizard_project.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wizard_project.Classes.User;
import com.example.wizard_project.R;

import java.util.ArrayList;
import java.util.List;

/**
 * BrowseProfileAdapter is a custom ArrayAdapter for displaying user profiles in a ListView.
 * It binds user data to views within each list item.
 */
public class BrowseProfileAdapter extends ArrayAdapter<User> {
    private List<User> profileList;
    private Context context;

    /**
     * Constructor for initializing the adapter with context and profile data.
     *
     * @param context     The context used for layout inflation.
     * @param profileList The list of User objects to display.
     */
    public BrowseProfileAdapter(Context context, ArrayList<User> profileList) {
        super(context, 0, profileList);
        this.context = context;
        this.profileList = profileList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate the item layout if it doesn't exist
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_profile_layout, parent, false);
        }

        // Get the User object for the current position in the list
        User profile = profileList.get(position);

        // Find the TextViews and ImageView in the item layout
        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        TextView Usertypetextview = convertView.findViewById(R.id.UserType);

        // Bind user data to views
        nameTextView.setText(profile.getName());
        Usertypetextview.setText(profile.getUserToString());

        return convertView;
    }
}
