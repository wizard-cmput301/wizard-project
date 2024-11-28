package com.example.wizard_project.Classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a geographical location using latitude and longitude coordinates.
 * This class implements Parcelable to allow passing objects between components.
 *
 * @see <a href="https://googleapis.github.io/googleapis/java/all/latest/apidocs/com/google/type/LatLng.html">LatLng</a>
 */
public class LatLng implements Parcelable {
    /**
     * CREATOR field for Parcelable implementation.
     */
    public static final Creator<LatLng> CREATOR = new Creator<LatLng>() {
        @Override
        public LatLng createFromParcel(Parcel in) {
            return new LatLng(in);
        }

        @Override
        public LatLng[] newArray(int size) {
            return new LatLng[size];
        }
    };
    private final double latitude;
    private final double longitude;

    /**
     * Constructs a new LatLng object with the specified latitude and longitude.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     */
    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructs a LatLng object from a Parcel.
     *
     * @param in The Parcel containing LatLng data.
     */
    protected LatLng(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    /**
     * Returns the latitude of the location.
     *
     * @return The latitude value.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude of the location.
     *
     * @return The longitude value.
     */
    public double getLongitude() {
        return longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes the LatLng object data to a Parcel.
     *
     * @param dest  The Parcel to write the data into.
     * @param flags Additional flags for the writing process.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
