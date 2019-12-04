package com.example.parkingspotlocatorapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GeofencingClient mGeofencingClient;
    private GoogleMap mMap;
    private static final String TAG = "MapsActivity";
    private ArrayList<Geofence> mGeofenceList;
    private ArrayList<MyGeofence> mMyGeofenceList;
    public static final int PERMISSION_REQUEST_CODE = 1;
    private Marker mCurrentLocation;
    public int spinner1;
    public int spinner2;
    public int spinner3;

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            double lat = intent.getDoubleExtra("latitude", 0);
            double lon = intent.getDoubleExtra("longitude", 0);
            int complete = intent.getIntExtra("complete", 0);

            if (complete == 1) {
                LatLng latLng = new LatLng(lat, lon);
                updateMarker(latLng);
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        checkPermissions();

        Button backToMain;
        backToMain = findViewById(R.id.backToMain);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (MapsActivity.this, MainActivity.class));
            }
        });

        //retrieve shared preferences from main activity
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        spinner1 = sharedPreferences.getInt("spinner1", 0);
        spinner2 = sharedPreferences.getInt("spinner2", 0);
        spinner3 = sharedPreferences.getInt("spinner3", 0);
    }

    private void checkPermissions() {

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        startLocationService();
        new GeofenceCreationAsyncTask().execute();


    }

    private void startLocationService() {
        Intent startLocationService = new Intent(
                this,
                LocationService.class
        );
        startService(startLocationService);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        registerReceiver(receiver,
                new IntentFilter("com.example.parkingspotlocatorapp"));
    }

    @Override
    protected void onPause() {

        if (receiver != null) {
            unregisterReceiver(receiver);
        }

        Intent intent = new Intent(this, LocationService.class);
        stopService(intent);
        super.onPause();

    }

    private void updateMarker(LatLng latLng) {

        if (mCurrentLocation == null) {
            mCurrentLocation = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("You")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );
        } else {
            mCurrentLocation.setPosition(latLng);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }

    class GeofenceCreationAsyncTask extends AsyncTask<Void, Void, Void> {

        PendingIntent geofencePendingIntent;

        @Override
        protected void onPreExecute() {
            // initialize geofence list before starting background task
            mGeofenceList = new ArrayList<>();
            mMyGeofenceList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            createGeofenceObjects();

            // Permissions are requested in OnCreate so they will definitely have been given
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }

            mGeofencingClient.addGeofences(getGeofencingRequest(),
                    getGeofencePendingIntent()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i(TAG, "onSuccess: Geofences added");
                    Toast.makeText(
                            getApplicationContext(),
                            "Geofence(s) successfully added.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "There was a problem adding the geofences.",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            placeGeofencesOnMap();
        }

        private PendingIntent getGeofencePendingIntent() {

            // The PendingIntent is basically a singleton
            if (geofencePendingIntent != null) {
                return geofencePendingIntent;
            } else {
                Intent intent = new Intent(MapsActivity.this,
                        GeofenceTransitionsService.class);
                geofencePendingIntent = PendingIntent.getService(MapsActivity.this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            return geofencePendingIntent;
        }

        private GeofencingRequest getGeofencingRequest() {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(mGeofenceList);

            return builder.build();
        }

        private void createGeofenceObjects() {

            // Create array of transition events
            ArrayList<Integer> transitionEvents = new ArrayList<>();
            transitionEvents.add(Geofence.GEOFENCE_TRANSITION_ENTER);
            //transitionEvents.add(Geofence.GEOFENCE_TRANSITION_EXIT);

            // Initialize geofences
            MyGeofence lot1Fence = new MyGeofence("Lot 47",
                    33.3061688,
                    -111.6745582,
                    80,
                    Geofence.NEVER_EXPIRE,
                    transitionEvents
            );

            MyGeofence lot2Fence = new MyGeofence("Lot 37",
                    33.304073,
                    -111.678383,
                    80,
                    Geofence.NEVER_EXPIRE,
                    transitionEvents);

            MyGeofence lot3Fence = new MyGeofence("Lot 24",
                    1,
                    1,
                    1,
                    Geofence.NEVER_EXPIRE,
                    transitionEvents);

            //add geofence(s) to master lists & maps view, only if they're chosen by the user in preferences
            switch (spinner1) {
                case 0:
                    break;
                case 1:
                    mMyGeofenceList.add(lot1Fence);
                    break;
                case 2:
                    mMyGeofenceList.add(lot2Fence);
                    break;
                case 3:
                    mMyGeofenceList.add(lot3Fence);
                    break;
            }
            switch (spinner2) {
                case 0:
                    break;
                case 1:
                    mMyGeofenceList.add(lot1Fence);
                    break;
                case 2:
                    mMyGeofenceList.add(lot2Fence);
                    break;
                case 3:
                    mMyGeofenceList.add(lot3Fence);
                    break;
            }
            switch (spinner3) {
                case 0:
                    break;
                case 1:
                    mMyGeofenceList.add(lot1Fence);
                    break;
                case 2:
                    mMyGeofenceList.add(lot2Fence);
                    break;
                case 3:
                    mMyGeofenceList.add(lot3Fence);
                    break;
            }

            for (MyGeofence geofence : mMyGeofenceList) {
                mGeofenceList.add(geofence.createGeofence());
            }

        }
    }

    private void placeGeofencesOnMap() {

        for (MyGeofence geofence : mMyGeofenceList) {
            updateMap(geofence);
        }
    }

    private void updateMap(MyGeofence geofence) {

        LatLng latLng = new LatLng(geofence.getLatitude(), geofence.getLongitude());

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(geofence.getRequestId())
        );

        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(geofence.getRadius())
                .strokeWidth(2)
                .strokeColor(Color.BLUE)
                .fillColor(Color.parseColor("#200084d3")) //#AARRGGBB AA = transparency
                .visible(true)
        );
    }
}
