package com.fuat.locationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private Marker marker;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            double latitude = intent.getDoubleExtra("latitude", 0.0);
            double longitude = intent.getDoubleExtra("longitude", 0.0);

            if (marker != null) {
                marker.remove();
            }

            LatLng latLng = new LatLng(latitude, longitude);
            MarkerOptions markerOptions = new MarkerOptions().title("Buradasınız!").position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            marker = googleMap.addMarker(markerOptions);
            TextView text = (TextView) findViewById(R.id.textView);
            String locationText = getString(R.string.koordinatlar, latitude, longitude);
            text.setText(locationText);
            //googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Start LocationService
        Intent i = new Intent(this, LocationService.class);
        startService(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Constants.SERVICE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setAllGesturesEnabled(true);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);

        try {
            this.googleMap.setMyLocationEnabled(true); // Show current location on the map(the blue dot)
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), "Couldn't get location access permission!", Toast.LENGTH_LONG).show();
        }

        // Create marker which located @KOU
        LatLng kouKampus = new LatLng(40.823650, 29.921951);
        MarkerOptions markerAtKou = new MarkerOptions().position(kouKampus).title("Kocaeli Üniversitesi(Umuttepe)");
        markerAtKou.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

        // Add this marker to the map
        this.googleMap.addMarker(markerAtKou);
    }
}