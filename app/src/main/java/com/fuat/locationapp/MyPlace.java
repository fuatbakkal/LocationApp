package com.fuat.locationapp;

import com.google.android.gms.maps.model.LatLng;

public class MyPlace {

    private String id, name;
    private double latitude, longitude;

    public MyPlace(String id, String name, LatLng latLng) {
        this.id = id;
        this.name = name;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof MyPlace)) return false;
        MyPlace o = (MyPlace) obj;
        return o.getId().equals(this.getId());
    }

    public double getLongitude() {
        return longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }
}
