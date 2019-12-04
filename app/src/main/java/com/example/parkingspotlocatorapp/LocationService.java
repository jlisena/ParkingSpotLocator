package com.example.parkingspotlocatorapp;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service {

    FusedLocationProviderClient mLocationProviderClient;
    LocationRequest mLocationRequest;
    LocationMonitoringThread thread;
    LocationCallback mLocationCallback;
    private static final String TAG = "LocationService";
    private static double longitude;
    private static double latitude;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        startForeground(1, createNotification());
        getLocationUpdates();
        thread = new LocationMonitoringThread();
        thread.start();
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                sendNewLocationBroadcast(locationResult);
            }
        };
        return START_STICKY;
    }

    private void getLocationUpdates() {

        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);

        }

    private void sendNewLocationBroadcast(LocationResult result){


        Intent intent = new Intent("com.example.parkingspotlocatorapp");

        latitude = result.getLastLocation().getLatitude();
        longitude = result.getLastLocation().getLongitude();
        int complete = 1;

        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("complete", complete);

        sendBroadcast(intent);

        Log.i(TAG, "sendNewLocationBroadcast: " + latitude + "," + longitude);

    }

    public static double getLongitude() {
        return longitude;
    }

    public static double getLatitude() {
        return latitude;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification(){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                ParkingSpotLocatorApp.getLocationChannelID())
                .setSmallIcon(R.drawable.ic_location_notification_icon)
                .setContentTitle("ParkingSpotLocator")
                .setContentText("This app is currently monitoring your location!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }

    class LocationMonitoringThread extends Thread{

        Looper looper;

        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            getLocationUpdates();
            looper = Looper.myLooper();
            looper.prepare();
            mLocationProviderClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    looper
            );

            looper.loop();
        }

    }

}
