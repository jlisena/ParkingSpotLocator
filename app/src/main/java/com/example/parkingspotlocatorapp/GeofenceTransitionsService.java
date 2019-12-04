package com.example.parkingspotlocatorapp;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeofenceTransitionsService extends IntentService {

    public FirebaseDatabase database = FirebaseDatabase.getInstance();
    public DatabaseReference parkingLot1 = database.getReference("parkingLot1");
    public DatabaseReference parkingLot2 = database.getReference("parkingLot2");

    public static final String TAG = "GeofenceTransitionSer";
    public static final int NOTIFICATION_CODE = 1000;
    public String geofenceLocationString;
    public String geofenceTransitionString;
    public String errorMessage;
    public int readLot1;
    public int readLot2;
    public int readLot3;
    public final Double lot1Capacity = 10.0;
    public final Double lot2Capacity = 8.0;
    public int spinner1;
    public int spinner2;
    public int spinner3;
    public boolean switch2;


    private TextToSpeech textToSpeechSystem;


    //lot1 (lot 47)
    public double lat1 = 33.3061688;
    public double lon1 = -111.6745582;
    //lot2 (lot 37)
    public double lat2 = 33.304073;
    public double lon2 = -111.678383;
    //lot3 (lot 33, doesnt exist)
    public double lat3 = 1;
    public double lon3 = 1;
    //current lot
    public double currentLat = LocationService.getLatitude();
    public double currentLon = LocationService.getLongitude();

    // recommendation scores to be refferend later on
    public Double lot1RecScore = 0.0;
    public Double lot2RecScore = 0.0;
    public String recommend;
    public int readRecommendLotSpots;

    public GeofenceTransitionsService() {
        super(TAG);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        //retrieve shared preferences from main activity
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        spinner1 = sharedPreferences.getInt("spinner1", 0);
        spinner2 = sharedPreferences.getInt("spinner2", 0);
        spinner3 = sharedPreferences.getInt("spinner3", 0);
        switch2 = sharedPreferences.getBoolean("switch2", false);
    }

    public static double distance(double firstLat, double secondLat, double firstLon, double secondLon) {
        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(secondLat - firstLat);
        double lonDistance = Math.toRadians(secondLon - firstLon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(firstLat)) * Math.cos(Math.toRadians(secondLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);
        distance = Math.sqrt(distance) * 3.2808; //convert to feet
        return Math.round(distance * 100.0) / 100.0; //round to 2 decimals
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent: ");
        handleTransition(intent);
    }

    private Notification createNotification(String geofenceLocation, String geofenceTransitionType, Integer readLot1, Integer readLot2, Integer readLot3) {
        String lot1Choice = "";
        String lot2Choice = "";
        String lot3Choice = "";
        Double lot1Percentage = (readLot1 / lot1Capacity) * 100;
        Double lot2Percentage = (readLot2 / lot2Capacity) * 100;
        switch (spinner1) {
            case 0:
                lot1Choice = "";
                break;
            case 1:
                lot1Choice = "\n[#1]Parking Lot 47:\nOpen: " + readLot1 + "/" + Math.round(lot1Capacity) + " (" + Math.round(lot1Percentage) +  "% full)\nDistance: " + distance(lat1, currentLat, lon1, currentLon) + " ft";
                break;
            case 2:
                lot1Choice = "\n[#1]Parking Lot 37:\nOpen: " + readLot2 + "/" + Math.round(lot2Capacity) + " (" + Math.round(lot2Percentage) +  "% full)\nDistance: " + distance(lat2, currentLat, lon2, currentLon) + " ft";
                break;
            case 3:
                lot1Choice = "\n[#1]Parking Lot 24:\nOpen: " + readLot3 + "\nDistance: " + distance(lat3, currentLat, lon3, currentLon) + " ft";
                break;
        }
        switch (spinner2) {
            case 0:
                lot2Choice = "";
                break;
            case 1:
                lot2Choice = "\n[#2]Parking Lot 47:\nOpen: " + readLot1 + "/" + Math.round(lot1Capacity) + " (" + Math.round(lot1Percentage) +  "% full)\nDistance: " + distance(lat1, currentLat, lon1, currentLon) + " ft";
                break;
            case 2:
                lot2Choice = "\n[#2]Parking Lot 37:\nOpen: " + readLot2 + "/" + Math.round(lot2Capacity) + " (" + Math.round(lot2Percentage) +  "% full)\nDistance: " + distance(lat2, currentLat, lon2, currentLon) + " ft";
                break;
            case 3:
                lot2Choice = "\n[#2]Parking Lot 24:\nOpen: " + readLot3 + "\nDistance: " + distance(lat3, currentLat, lon3, currentLon) + " ft";
                break;
        }
        switch (spinner3) {
            case 0:
                lot3Choice = "";
                break;
            case 1:
                lot3Choice = "\n[#3]Parking Lot 47:\nOpen: " + readLot1 + "/" + Math.round(lot1Capacity) + " (" + Math.round(lot1Percentage) +  "% full)\nDistance: " + distance(lat1, currentLat, lon1, currentLon) + " ft";
                break;
            case 2:
                lot3Choice = "\n[#3]Parking Lot 37:\nOpen: " + readLot2 + "/" + Math.round(lot2Capacity) + " (" + Math.round(lot2Percentage) +  "% full)\nDistance: " + distance(lat2, currentLat, lon2, currentLon) + " ft";
                break;
            case 3:
                lot3Choice = "\n[#3]Parking Lot 24:\nOpen: " + readLot3 + "\nDistance: " + distance(lat3, currentLat, lon3, currentLon) + " ft";
                break;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                ParkingSpotLocatorApp.getGeofenceTransitionChannelId())
                .setSmallIcon(R.drawable.ic_location_notification_icon)
                //.setContentTitle(geofenceLocation + " transition event.")
                .setContentTitle(geofenceTransitionType + ": " + geofenceLocation)
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("You just " + geofenceTransitionType + " " + geofenceLocation + "\nOpen: " + readLot1))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(lot1Choice + "\n" + lot2Choice + "\n" + lot3Choice + "\n" + "We recommend Lot " + recommend + "."))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        return builder.build();
    }

    private void handleTransition(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Handle any errors by logging error code
        if (geofencingEvent.hasError()) {
            errorMessage = String.valueOf(geofencingEvent.getErrorCode());
            Log.e(TAG, "HandleTransition: " + errorMessage);
            return;
        }

        // Get transition type
        int geofenceTransitionCode = geofencingEvent.getGeofenceTransition();


// TTS module with final recommendation
        textToSpeechSystem = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                // user preference 1
                if (spinner1 == 1) {
                    lot1RecScore = lot1RecScore + 0.40;
                }
                else if (spinner1 == 2) {
                    lot2RecScore = lot2RecScore + 0.40;
                }

                // user preference 2
                if (spinner2 == 1) {
                    lot1RecScore = lot1RecScore + 0.25;
                }
                else if (spinner2 == 2) {
                    lot2RecScore = lot2RecScore + 0.25;
                }

                // empty spots for lot 1
                if (readLot1 > 10) {
                    lot1RecScore = lot1RecScore + 0.35;
                }
                else {
                    if (readLot1 >= 5) {
                        lot1RecScore = lot1RecScore + 0.20;
                    }
                    else {
                        if (readLot1 >= 2) {
                            lot1RecScore = lot1RecScore + 0.10;
                        }
                        else {
                            if (readLot1 >= 0) {
                                lot1RecScore = lot1RecScore - 1.0;
                            }
                        }
                    }
                }

                // empty spots for lot 2
                if (readLot2 > 10) {
                    lot2RecScore = lot2RecScore + 0.35;
                }
                else {
                    if (readLot2 >= 5) {
                        lot2RecScore = lot2RecScore + 0.20;
                    }
                    else {
                        if (readLot2 >= 2) {
                            lot2RecScore = lot2RecScore + 0.10;
                        }
                        else {
                            if (readLot2 >= 0) {
                                lot2RecScore = lot2RecScore - 1.0;
                            }
                        }
                    }
                }


                // distance calculation for lot 1
                if (distance(lat1, currentLat, lon1, currentLon) < 499) {
                    lot1RecScore = lot1RecScore + 0.25;
                }
                else {
                    if (distance(lat1, currentLat, lon1, currentLon) < 800) {
                        lot1RecScore = lot1RecScore + 0.15;
                    }
                    else {
                        if (distance(lat1, currentLat, lon1, currentLon) < 1750) {
                            lot1RecScore = lot1RecScore + 0.10;
                        }
                        else {
                            if (distance(lat1, currentLat, lon1, currentLon) < 2500) {
                                lot1RecScore = lot1RecScore + 0.05;
                            }
                            else {
                                if (distance(lat1, currentLat, lon1, currentLon) < 5000) {
                                    lot1RecScore = lot1RecScore + 0.0;
                                }
                            }
                        }
                    }
                }

                // distance calculation for lot 2
                if (distance(lat2, currentLat, lon2, currentLon) < 499) {
                    lot2RecScore = lot2RecScore + 0.25;
                }
                else {
                    if (distance(lat2, currentLat, lon2, currentLon) < 800) {
                        lot2RecScore = lot2RecScore + 0.15;
                    }
                    else {
                        if (distance(lat2, currentLat, lon2, currentLon) < 1750) {
                            lot2RecScore = lot2RecScore + 0.10;
                        }
                        else {
                            if (distance(lat2, currentLat, lon2, currentLon) < 2500) {
                                lot2RecScore = lot2RecScore + 0.05;
                            }
                            else {
                                if (distance(lat1, currentLat, lon1, currentLon) < 5000) {
                                    lot2RecScore = lot2RecScore + 0.0;
                                }
                            }
                        }
                    }
                }

                // recommendation calculation
                if (lot1RecScore > lot2RecScore) {
                    recommend = "47";
                    readRecommendLotSpots = readLot1;
                }
                else if (lot1RecScore < lot2RecScore) {
                    recommend = "37";
                    readRecommendLotSpots = readLot2;

                }
                Log.d(TAG, "Recommendation Score of lot 1: " + lot1RecScore);
                Log.d(TAG, "Recommendation Score of lot 2: " + lot2RecScore);

                // if TTS is on
                if ((status == TextToSpeech.SUCCESS) && (switch2)){
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String recommendation = "Parking Spot Locator recommends AGBC lot " + recommend + " with " + readRecommendLotSpots +  " spots open.";
                    textToSpeechSystem.speak(recommendation, TextToSpeech.QUEUE_ADD, null);
                }
            }
        });


        // Verify that the transition is one we're interested in
        if (geofenceTransitionCode == Geofence.GEOFENCE_TRANSITION_ENTER) { //|| geofenceTransitionCode == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // retrieve data from Firebase and read parking lot 1 data
            parkingLot1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    readLot1 = dataSnapshot.getValue(Integer.class);
                    Log.d(TAG, "Value is: " + readLot1);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            // retrieve data from Firebase and read parking lot 2 data
            parkingLot2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    readLot2 = dataSnapshot.getValue(Integer.class);
                    Log.d(TAG, "Value is: " + readLot2);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });



            // this is needed because the Firebase data retrieval uses asynchronous loading
            // bad programming practice -- fix in future?
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



            // Get location string of transition
            List<Geofence> transitionEventList = geofencingEvent.getTriggeringGeofences();
            for(Geofence geofence : transitionEventList){
                geofenceLocationString = geofence.getRequestId();
            }

            // get transition type string of transition
            switch (geofenceTransitionCode) {

                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    geofenceTransitionString = "Entered";
                    break;

                /*case Geofence.GEOFENCE_TRANSITION_EXIT:
                    geofenceTransitionString = "exited";
                    break;*/

                default:
                    Log.i(TAG, "HandleTransition: No valid transition type.");

            }
            Log.i(TAG, "handleTransition: A transition occurred at " + geofenceLocationString);
            //Log.i(TAG, "Testing update of file " + readLotString);

            // Create custom notification using location and transition strings
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_CODE,
                    createNotification(geofenceLocationString, geofenceTransitionString, readLot1, readLot2, readLot3));
        }
    }
}