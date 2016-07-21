package com.fuat.locationapp;

public final class Constants {

    public static final String TAG = "*LOCATIONAPP*";
    public static final String SERVICE_ACTION = "LocationServiceAction";
    public static final long UPDATE_INTERVAL = 20000;
    public static final long FASTEST_INTERVAL = 15000;

    private Constants() {
        throw new AssertionError();
    }
}