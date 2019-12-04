package com.example.parkingspotlocatorapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

public class ParkingSpotLocatorApp extends Application {

    public static final String LOCATION_CHANNEL_ID = "location_channel_id";
    public static final String GEOFENCE_TRANSITION_CHANNEL_ID = "Geofence_transition_channel_id";
    public static final String TAG = "ParkingSpotLocator";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        createNotificationChannel(LOCATION_CHANNEL_ID,
                "location_channel",
                NotificationManager.IMPORTANCE_DEFAULT,
                "This channel details system location services.");
        createNotificationChannel(GEOFENCE_TRANSITION_CHANNEL_ID,
                "geofence_transition_channel",
                NotificationManager.IMPORTANCE_DEFAULT,
                "This channel details geofence notifications.");

        Log.i(TAG, "createNotificationChannels: ");
    }

    private void createNotificationChannel(String channelID, String channelName,
                                                          int importance, String description){

        NotificationManager manager;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelID,
                    channelName,
                    importance
            );
            channel.setDescription(description);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static String getLocationChannelID(){
        return LOCATION_CHANNEL_ID;
    }

    public static String getGeofenceTransitionChannelId(){
        return GEOFENCE_TRANSITION_CHANNEL_ID;
    }
}
