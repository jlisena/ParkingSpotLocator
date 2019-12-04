package com.example.parkingspotlocatorapp;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;


public class MyGeofence {

    private String requestId;
    private double latitude;
    private double longitude;
    private float radius;
    private long expirationDuration;

    public MyGeofence(String requestId, double latitude, double longitude, float radius,
                      long expirationDuration, ArrayList<Integer> transitionTypes){
        this.requestId = requestId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.expirationDuration = expirationDuration;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId(){ return requestId; }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getExpirationDuration() {
        return expirationDuration;
    }

    public void setExpirationDuration(long expirationDuration) {
        this.expirationDuration = expirationDuration;
    }

    public float getRadius (){ return radius; }

    public void setRadius(float radius){this.radius = radius; }


    public Geofence createGeofence(){

        Geofence.Builder builder = new Geofence.Builder();
        builder.setRequestId(requestId)
                .setExpirationDuration(expirationDuration)
                .setCircularRegion(latitude,
                        longitude,
                        radius
                        )
                .setExpirationDuration(expirationDuration)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER); //| Geofence.GEOFENCE_TRANSITION_EXIT);

        return builder.build();
    }
}
