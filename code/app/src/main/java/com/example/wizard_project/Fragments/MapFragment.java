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

/**
 * MapFragment displays a static world map with markers indicating specified locations.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Equirectangular_projection">Equirectangular projection</a>
 */
public class MapFragment extends Fragment {
    private static final String ARG_LOCATIONS = "locations";

    private static final int MARKER_RADIUS = 10; // Marker size
    private static final int MARKER_COLOR = Color.RED; // Marker color

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
            drawMarkersOnMap(mutableBitmap, mapImageView);
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
     * Draws markers on the map based on provided location data.
     *
     * @param bitmap       The mutable bitmap of the map.
     * @param mapImageView The ImageView displaying the map.
     */
    private void drawMarkersOnMap(Bitmap bitmap, ImageView mapImageView) {
        if (locations == null || locations.length == 0) {
            return;
        }

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(MARKER_COLOR);
        paint.setStyle(Paint.Style.FILL);

        for (LatLng location : locations) {
            PointF point = mapToImageCoordinates(location.getLatitude(), location.getLongitude(), bitmap.getWidth(), bitmap.getHeight());
            canvas.drawCircle(point.x, point.y, MARKER_RADIUS, paint); // Draw marker
        }

        requireActivity().runOnUiThread(() -> mapImageView.setImageBitmap(bitmap));
    }

    /**
     * Converts latitude and longitude into pixel coordinates on the map image.
     *
     * @param latitude    The latitude of the location.
     * @param longitude   The longitude of the location.
     * @param imageWidth  The width of the map image.
     * @param imageHeight The height of the map image.
     * @return A PointF object representing the pixel coordinates on the map.
     */
    private PointF mapToImageCoordinates(double latitude, double longitude, int imageWidth, int imageHeight) {
        float x = (float) ((longitude + 180) / 360 * imageWidth);
        float y = (float) ((90 - latitude) / 180 * imageHeight);
        return new PointF(x, y);
    }
}
