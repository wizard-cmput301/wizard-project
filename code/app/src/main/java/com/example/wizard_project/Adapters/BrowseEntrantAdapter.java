package com.example.wizard_project.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.Shapeable;

import java.util.ArrayList;
import java.util.List;

/**
 * BrowseEntrantAdapter is a custom ArrayAdapter to display the list of entrants for an event.
 */
public class BrowseEntrantAdapter extends ArrayAdapter<User> {

    private List<User> userList;
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
        User currentUser = userList.get(position);

        userName.setText(currentUser.getName());
        selectionBox.setText("");

        return convertView;
    }
}