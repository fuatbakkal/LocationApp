package com.fuat.locationapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private boolean currentlyProcessingLocation = false;
    private GoogleApiClient googleApiClient;
    private LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
    private Intent intent = new Intent(Constants.SERVICE_ACTION);
    private Location previousLocation = null;
    private DatabaseReference mDatabase;

    private CountDownTimer timer = new CountDownTimer(600000, 1000) {
        @Override
        public void onTick(long l) {
            // ...
        }

        @Override
        public void onFinish() {
            long datetime = System.currentTimeMillis();
            mDatabase.child("locations").child(Long.toString(datetime)).child("latitude").setValue(previousLocation.getLatitude());
            mDatabase.child("locations").child(Long.toString(datetime)).child("longitude").setValue(previousLocation.getLongitude());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (!currentlyProcessingLocation) {
            currentlyProcessingLocation = true;
            startTracking();
        }
        return START_NOT_STICKY;
    }

    public boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private void startTracking() {
        Log.d(Constants.TAG, "startTracking");

        if (isGooglePlayServicesAvailable(this)) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }

        } else {
            Log.e(Constants.TAG, "Unable to connect google play services");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.v(Constants.TAG, "position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());

            intent.putExtra("latitude", location.getLatitude());
            intent.putExtra("longitude", location.getLongitude());
            lbm.sendBroadcast(intent);

            if (isBetterLocation(location, previousLocation)) {
                previousLocation = location;
                timer.cancel();
                timer.start();
            }
        }
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            Log.d(Constants.TAG, "stopLocationUpdates");
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(Constants.TAG, "onConnected");

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(Constants.UPDATE_INTERVAL);
        locationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(this.googleApiClient, locationRequest, this);
        } catch (SecurityException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(Constants.TAG, "onConnectionFailed");
        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(Constants.TAG, "GoogleApiClient connection has been suspend");
    }

    private boolean isBetterLocation(Location currentLocation, Location previousLocation) {

        return previousLocation == null || currentLocation.distanceTo(previousLocation) >= Constants.DISTANCE_GAP;
    }
}