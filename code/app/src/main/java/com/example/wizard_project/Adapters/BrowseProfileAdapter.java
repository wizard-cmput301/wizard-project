package com.example.wizard_project.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.wizard_project.R;
import com.example.wizard_project.Classes.User;

import java.util.List;

public class BrowseProfileAdapter extends RecyclerView.Adapter<BrowseProfileAdapter.ProfileViewHolder> {
    private List<User> profileList;

    public BrowseProfileAdapter(List<User> profileList) {
        this.profileList = profileList;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_profile_layout, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        User profile = profileList.get(position);
        holder.bind(profile);
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView UserTypeTextView;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            UserTypeTextView = itemView.findViewById(R.id.UserType);
        }

        public void bind(User profile) {
            nameTextView.setText(profile.getName());
            UserTypeTextView.setText(profile.getUserToString());
        }
    }
}