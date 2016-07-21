package com.fuat.locationapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private boolean currentlyProcessingLocation = false;
    private GoogleApiClient mGoogleApiClient;
    private LocalBroadcastManager mLbm = LocalBroadcastManager.getInstance(this);
    private Intent mIntent = new Intent(Constants.SERVICE_ACTION);
    private DatabaseReference mDatabase;
    private MyPlace previousPlace = null, currentPlace = null;
    private FirebaseUser user;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase.child("users").child(user.getUid()).child("name").setValue(user.getDisplayName());
        mDatabase.child("users").child(user.getUid()).child("e-mail").setValue(user.getEmail());

        if (!currentlyProcessingLocation) {
            currentlyProcessingLocation = true;
            startTracking();
        }

        return START_STICKY;
    }

    public boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private void startTracking() {
        Log.d(Constants.TAG, "startTracking");

        if (isGooglePlayServicesAvailable(this)) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
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
    public void onLocationChanged(Location currentLocation) {
        if (currentLocation != null) {
            mIntent.putExtra("latitude", currentLocation.getLatitude());
            mIntent.putExtra("longitude", currentLocation.getLongitude());
            mLbm.sendBroadcast(mIntent);

            long dateTime = System.currentTimeMillis();
            mDatabase.child("users").child(user.getUid()).child("current_location").child("latitude").setValue(currentLocation.getLatitude());
            mDatabase.child("users").child(user.getUid()).child("current_location").child("longitude").setValue(currentLocation.getLongitude());
            guessCurrentPlace();

            if (isNewPlace(currentPlace, previousPlace)) {
                mDatabase.child("places").child(currentPlace.getId()).child("name").setValue(currentPlace.getName());
                mDatabase.child("places").child(currentPlace.getId()).child("latitude").setValue(currentPlace.getLatitude());
                mDatabase.child("places").child(currentPlace.getId()).child("longitude").setValue(currentPlace.getLongitude());
                mDatabase.child("places").child(currentPlace.getId()).child("visitors").child(String.valueOf(dateTime)).setValue(user.getUid());
                mDatabase.child("users").child(user.getUid()).child("visited_places").child(String.valueOf(dateTime)).setValue(currentPlace.getId());
                previousPlace = currentPlace;
            }

            Log.v(Constants.TAG, "position: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + " accuracy: " + currentLocation.getAccuracy());
        }
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.d(Constants.TAG, "stopLocationUpdates");
            mGoogleApiClient.disconnect();
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
            LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, locationRequest, this);
        } catch (SecurityException e) {
            Log.e(Constants.TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopLocationUpdates();
        stopSelf();
        Log.e(Constants.TAG, "onConnectionFailed");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(Constants.TAG, "GoogleApiClient connection has been suspended");
    }

    private boolean isNewPlace(MyPlace currentPlace, MyPlace previousPlace) {
        if(previousPlace == null && currentPlace != null) return true;
        if(previousPlace != null && currentPlace != null) return !currentPlace.equals(previousPlace);

        return false;
    }

    private void guessCurrentPlace() {
        try {
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);

            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {

                    if (placeLikelihoods.getCount() > 0) {
                        currentPlace = new MyPlace(placeLikelihoods.get(0).getPlace().getId(),
                                placeLikelihoods.get(0).getPlace().getName().toString(),
                                placeLikelihoods.get(0).getPlace().getLatLng());
                    }

                    placeLikelihoods.release();
                }
            });
        } catch (SecurityException e) {
            Log.e(Constants.TAG, e.getLocalizedMessage());
        }
    }
}