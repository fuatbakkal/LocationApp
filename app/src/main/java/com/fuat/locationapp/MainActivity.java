package com.fuat.locationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private Marker marker;
    private DatabaseReference mDatabase;
    private List<Nodes> nodes = new ArrayList<>();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            double latitude = intent.getDoubleExtra("latitude", 0.0);
            double longitude = intent.getDoubleExtra("longitude", 0.0);

            Toast.makeText(getApplicationContext(), "Location Changed: " + Double.toString(latitude) + ", "
                    + Double.toString(longitude), Toast.LENGTH_LONG).show();

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

        // Get database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("locations").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int i = 0;
                        PolylineOptions strokes = new PolylineOptions();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            nodes.add(new Nodes(Double.parseDouble(child.child("latitude").getValue().toString()),
                                    Double.parseDouble(child.child("longitude").getValue().toString())));
                            googleMap.addMarker(new MarkerOptions().position(nodes.get(i).getlatLng()).title(".").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            strokes.add(nodes.get(i++).getlatLng());
                        }
                        strokes.width(5).color(Color.BLUE).geodesic(true);
                        googleMap.addPolyline(strokes);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("DB" + Constants.TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });

        String serverKey = "AIzaSyCJD59Y6U-R-MJ-yBv6E3Uh4_yCkor2B9U";
        LatLng origin = new LatLng(40.823650, 29.921951);
        LatLng destination = new LatLng(40.7734484, 29.9837398);

        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.getStatus().equals(RequestResult.OK)) {
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);
                            ArrayList<LatLng> pointList = leg.getDirectionPoint();

                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), pointList, 5, Color.RED);
                            googleMap.addPolyline(polylineOptions);
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // ...
                    }
                });

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