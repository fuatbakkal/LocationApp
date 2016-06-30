package com.fuat.locationapp;

public final class Constants {

    public static final String TAG = "*************";
    public static final String SERVICE_ACTION = "LocationServiceAction";
    public static final String LOG_APP = "sdcard/log_app.txt";
    public static final String LOG_SERVICE = "sdcard/log_service.txt";
    public static final long UPDATE_INTERVAL = 5000;
    public static final long FASTEST_INTERVAL = 3000;
    public static final int DISTANCE_GAP = 200; //distance gap between locations (in meters)

    private Constants() {
        throw new AssertionError();
    }
}