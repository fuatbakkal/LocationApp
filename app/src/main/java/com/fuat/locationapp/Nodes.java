package com.fuat.locationapp;

import com.google.android.gms.maps.model.LatLng;

/* This class will be used for creating graph */
public class Nodes {

    private double latitude, longitude;
    private LatLng latLng;

    public Nodes(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.latLng = new LatLng(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getlatLng() {
        return latLng;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Nodes)) return false;
        Nodes o = (Nodes) obj;
        return o == this;
    }
}