package com.example.wizard_project.Adapters;

import android.content.Context;
import android.media.Image;
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
import com.example.wizard_project.Classes.ImageHolder;
import com.example.wizard_project.R;

import java.util.ArrayList;
import java.util.List;

public class BrowseImageAdapter extends ArrayAdapter<ImageHolder> {

    private List<ImageHolder> imagelist;
    private Context context;

    public BrowseImageAdapter(Context context, ArrayList<ImageHolder> imagelist) {
        super(context, 0, imagelist);
        this.context = context;
        this.imagelist = imagelist;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Inflate the item layout if it doesn't exist
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_image_layout, parent, false);
        }

        // Get the User object for this position
        ImageHolder image_obj = imagelist.get(position);

        // Find the TextViews and ImageView in the item layout
        ImageView image = convertView.findViewById(R.id.AdminImageView);
        TextView imageName = convertView.findViewById(R.id.AdminImageName);

        // Set data to views
        if (!image_obj.getImageUrl().equals("")) {
            Uri imageUri = Uri.parse(image_obj.getImageUrl());
            Glide.with(context).load(imageUri).circleCrop().into(image);
        }
        imageName.setText(image_obj.getImagePath());
        return convertView;
    }
}
