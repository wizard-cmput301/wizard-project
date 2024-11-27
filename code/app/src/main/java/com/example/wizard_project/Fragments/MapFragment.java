package com.example.wizard_project.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wizard_project.Classes.LatLng;
import com.example.wizard_project.R;

public class MapFragment extends Fragment {
    private static final String ARG_LOCATIONS = "locations";
    private LatLng[] locations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        ImageView mapImageView = view.findViewById(R.id.mapImageView);

        // Load the static map and draw the locations
        mapImageView.post(() -> {
            Bitmap mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.world_map);
            Bitmap mutableBitmap = mapBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);

            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);

            if (locations != null) {
                for (LatLng location : locations) {
                    PointF point = mapToImageCoordinates(location.getLatitude(), location.getLongitude(), mapBitmap.getWidth(), mapBitmap.getHeight());
                    canvas.drawCircle(point.x, point.y, 10, paint); // Draw a marker
                }
            }

            mapImageView.setImageBitmap(mutableBitmap);
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            locations = MapFragmentArgs.fromBundle(getArguments()).getLocations();
        }
    }

    /**
     * Converts latitude and longitude into pixel coordinates on the map image.
     */
    private PointF mapToImageCoordinates(double latitude, double longitude, int imageWidth, int imageHeight) {
        // Adjust scaling factors based on your map's projection
        float x = (float) ((longitude + 180) / 360 * imageWidth);
        float y = (float) ((90 - latitude) / 180 * imageHeight);
        return new PointF(x, y);
    }
}