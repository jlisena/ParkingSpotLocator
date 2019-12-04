package com.example.parkingspotlocatorapp;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.content.SharedPreferences;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch;
import android.util.Log;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //init spinner data
    public static final String SPINNER1 = "spinner1";
    private Spinner spinner1;
    public static final String SPINNER2 = "spinner2";
    private Spinner spinner2;
    public static final String SPINNER3 = "spinner3";
    private Spinner spinner3;

    //init switch data
    public static final String SWITCH1 = "switch1";
    public static final String SWITCH2 = "switch2";
    private Switch switch1;
    private Switch switch2;

    //file to save cached data
    public static final String SHARED_PREFS = "sharedPrefs";

    //load data current state to shared preferences
    private int lot1_spinner_status;
    private int lot2_spinner_status;
    private int lot3_spinner_status;
    private boolean alert_status;
    private boolean tts_status;
    public boolean checkDuplicates = false;

    //settings button
    public ImageButton btn_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        spinner1 = findViewById(R.id.lot1_spinner);
        spinner2 = findViewById(R.id.lot2_spinner);
        spinner3 = findViewById(R.id.lot3_spinner);

        switch1 = findViewById(R.id.alert_status_switch);
        switch2 = findViewById(R.id.tts_status_switch);

        //spinner array color/text
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.parking_lots, R.layout.spinner_item);
        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter);
        spinner3.setAdapter(adapter);

        //update button function
        Button saveButton;
        saveButton = findViewById(R.id.updateBtn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
                readSharedPreferences();
            }
        });

        //maps view button function
        Button mapsBtn;
        mapsBtn = findViewById(R.id.mapsBtn);

        //settings button nav
        btn_settings = findViewById(R.id.btn_settings);

        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });


        //if alert status is false, do not open maps
        if (!sharedPreferences.getBoolean(SWITCH1, false)) {
            mapsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this, "Turn Alerts On & Update.", Toast.LENGTH_LONG).show();
                    spinner1.setEnabled(false);
                    spinner2.setEnabled(false);
                    spinner3.setEnabled(false);
                }
            });
        }
        //checks for duplicate spinner items
        else if (((sharedPreferences.getInt(SPINNER1, 0)) == (sharedPreferences.getInt(SPINNER2, 0))) ||
                ((sharedPreferences.getInt(SPINNER2, 0)) == (sharedPreferences.getInt(SPINNER3, 0))) ||
                ((sharedPreferences.getInt(SPINNER1, 0)) == (sharedPreferences.getInt(SPINNER3, 0)))) {
            //if duplicate spinner items are "None", then ignore and proceed
            if (((sharedPreferences.getInt(SPINNER1, 0)) == 0) && ((sharedPreferences.getInt(SPINNER2, 0)) == 0) ||
                ((sharedPreferences.getInt(SPINNER2, 0)) == 0) && ((sharedPreferences.getInt(SPINNER3, 0)) == 0) ||
                ((sharedPreferences.getInt(SPINNER1, 0)) == 0) && ((sharedPreferences.getInt(SPINNER3, 0)) == 0)) {
                //but, if ALL spinners are "None", then ask to choose a lot
                if (((sharedPreferences.getInt(SPINNER1, 0)) == 0) && ((sharedPreferences.getInt(SPINNER2, 0)) == 0) && ((sharedPreferences.getInt(SPINNER3,0) == 0))) {
                    mapsBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(MainActivity.this, "Choose Your Preferred Lot(s).", Toast.LENGTH_LONG).show();
                            checkDuplicates = false;
                        }
                    });
                }
                else {
                    mapsBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(MainActivity.this, MapsActivity.class));
                        }
                    });
                }
            }
            else {
                mapsBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, "No Duplicates. Update Your Preferences.", Toast.LENGTH_LONG).show();
                        checkDuplicates = false;
                    }
                });
            }
        }
        //if alert status is on & no duplicates are found, open maps
        else {
            mapsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
                    spinner1.setEnabled(true);
                    spinner2.setEnabled(true);
                    spinner3.setEnabled(false);
                }
            });
        }

        //load in spinner items to a string list
        final String[] parking_lots = getResources().getStringArray(R.array.parking_lots);
        final List<String> parkingList = new ArrayList<>(Arrays.asList(parking_lots));

        //disable spinner items if parking lot is offline & update views
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,R.layout.spinner_item,parkingList){
            @Override
            public boolean isEnabled(int position){
                //disable spinner item number 4 (0,1,2,[3])
                return position != 3;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView updateTv = (TextView) view;
                String disabledTv = parking_lots[position] + " (Offline)";
                if (position == 3) {
                    //set the disabled item text/color/typeface
                    updateTv.setTextColor(Color.parseColor("#B5454D"));
                    updateTv.setTypeface(updateTv.getTypeface(), Typeface.ITALIC);
                    updateTv.setText(disabledTv);
                    updateTv.setBackgroundColor(Color.parseColor("#272323"));
                }
                else {
                    updateTv.setTypeface(updateTv.getTypeface(), Typeface.BOLD);
                    updateTv.setTextColor(Color.parseColor("#C1C1C1"));
                    updateTv.setBackgroundColor(Color.parseColor("#1D1D1D"));
                }
                return view;
            }
        };

        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner1.setAdapter(spinnerArrayAdapter);
        spinner2.setAdapter(spinnerArrayAdapter);
        spinner3.setAdapter(spinnerArrayAdapter);

        loadData();
        updateViews();

    }

    //save data states of all cached data
    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(SPINNER1,spinner1.getSelectedItemPosition());
        editor.putInt(SPINNER2,spinner2.getSelectedItemPosition());
        editor.putInt(SPINNER3,spinner3.getSelectedItemPosition());

        editor.putBoolean(SWITCH1, switch1.isChecked());
        editor.putBoolean(SWITCH2, switch2.isChecked());

        editor.apply();

        if (sharedPreferences.getBoolean(SWITCH1, false)) {
            Toast.makeText(this, "Saved.", Toast.LENGTH_SHORT).show();
            spinner1.setEnabled(true);
            spinner2.setEnabled(true);
            spinner3.setEnabled(false);
        }
        else {
            Toast.makeText(this, "Saved. Please Exit App.", Toast.LENGTH_SHORT).show();
            spinner1.setEnabled(false);
            spinner2.setEnabled(false);
            spinner3.setEnabled(false);
        }
    }

    //reload data to previous state
    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        lot1_spinner_status = sharedPreferences.getInt(SPINNER1,0);
        lot2_spinner_status = sharedPreferences.getInt(SPINNER2,0);
        lot3_spinner_status = sharedPreferences.getInt(SPINNER3,0);

        alert_status = sharedPreferences.getBoolean(SWITCH1, false);
        tts_status = sharedPreferences.getBoolean(SWITCH2, false);
    }

    //update previously selected data on launch
    public void updateViews() {

        spinner1.setSelection(lot1_spinner_status);
        spinner2.setSelection(lot2_spinner_status);
        spinner3.setSelection(lot3_spinner_status);

        switch1.setChecked(alert_status);
        switch2.setChecked(tts_status);
    }

    public void readSharedPreferences() {
        //load in spinner items to a string list
        String[] parking_lots = getResources().getStringArray(R.array.parking_lots);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        //print to log
        Log.i("Alert Status", "value=" + sharedPreferences.getBoolean(SWITCH1, false));
        Log.i("TTS Status", "value="  + sharedPreferences.getBoolean(SWITCH2, false));
        Log.i("PL1 Choice", "value="  + parking_lots[sharedPreferences.getInt(SPINNER1, 0)]);
        Log.i("PL2 Choice", "value="  + parking_lots[sharedPreferences.getInt(SPINNER2, 0)]);
        Log.i("PL3 Choice", "value="  + parking_lots[sharedPreferences.getInt(SPINNER3, 0)]);

        //maps view button function
        Button mapsBtn;
        mapsBtn = findViewById(R.id.mapsBtn);

        //if alert status is false, do not open maps
        if (!sharedPreferences.getBoolean(SWITCH1, false)) {
            mapsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this, "Turn Notifications On & Update.", Toast.LENGTH_LONG).show();
                    spinner1.setEnabled(false);
                    spinner2.setEnabled(false);
                    spinner3.setEnabled(false);
                }
            });
        }
        //checks for duplicate spinner items
        else if (((sharedPreferences.getInt(SPINNER1, 0)) == (sharedPreferences.getInt(SPINNER2, 0))) ||
                 ((sharedPreferences.getInt(SPINNER2, 0)) == (sharedPreferences.getInt(SPINNER3, 0))) ||
                 ((sharedPreferences.getInt(SPINNER1, 0)) == (sharedPreferences.getInt(SPINNER3, 0)))) {
            //if duplicate spinner items are "None", then ignore and proceed
            if (((sharedPreferences.getInt(SPINNER1, 0)) == 0) && ((sharedPreferences.getInt(SPINNER2, 0)) == 0) ||
                ((sharedPreferences.getInt(SPINNER2, 0)) == 0) && ((sharedPreferences.getInt(SPINNER3, 0)) == 0) ||
                ((sharedPreferences.getInt(SPINNER1, 0)) == 0) && ((sharedPreferences.getInt(SPINNER3, 0)) == 0)) {
                //but, if ALL spinners are "None", then ask to choose a lot
                if (((sharedPreferences.getInt(SPINNER1, 0)) == 0) && ((sharedPreferences.getInt(SPINNER2, 0)) == 0) && ((sharedPreferences.getInt(SPINNER3,0) == 0))) {
                    mapsBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(MainActivity.this, "Choose Preferred Lot(s) & Preferences.", Toast.LENGTH_LONG).show();
                            checkDuplicates = false;
                        }
                    });
                }
                else {
                    mapsBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(MainActivity.this, MapsActivity.class));
                        }
                    });
                }
            }
            else {
                mapsBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, "No Duplicates. Update Your Preferences.", Toast.LENGTH_LONG).show();
                        checkDuplicates = false;
                    }
                });
            }
        }
        //if alert status is on & no duplicates are found, open maps
        else {
            mapsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
                }
            });
        }
    }
}