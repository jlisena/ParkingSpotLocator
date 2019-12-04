package com.example.parkingspotlocatorapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    //init spinner data
    public static final String SPINNER1 = "settings_spinner1";
    private Spinner spinner1;
    public static final String SPINNER2 = "settings_spinner2";
    private Spinner spinner2;
    public static final String SPINNER3 = "settings_spinner3";
    private Spinner spinner3;


    //file to save cached data
    public static final String SHARED_PREFS = "sharedPrefs";

    //load data current state to shared preferences
    private int lot1_spinner_status;
    private int lot2_spinner_status;
    private int lot3_spinner_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        ImageButton btn_back;
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            }
        });

        spinner1 = findViewById(R.id.choice1_spinner);
        spinner2 = findViewById(R.id.choice2_spinner);
        spinner3 = findViewById(R.id.choice3_spinner);

        //spinner array color/text
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.user_choices, R.layout.spinner_item);
        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter);
        spinner3.setAdapter(adapter);

        //update button function
        Button saveButton;
        saveButton = findViewById(R.id.updateBtn_settings);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
                readSharedPreferences();
            }
        });

        loadData();
        updateViews();

    }

    //save data states of all cached data
    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(SPINNER1, spinner1.getSelectedItemPosition());
        editor.putInt(SPINNER2, spinner2.getSelectedItemPosition());
        editor.putInt(SPINNER3, spinner3.getSelectedItemPosition());

        editor.apply();


        Toast.makeText(this, "Saved.", Toast.LENGTH_SHORT).show();
        spinner1.setEnabled(true);
        spinner2.setEnabled(true);
        spinner3.setEnabled(true);

    }

    //reload data to previous state
    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        lot1_spinner_status = sharedPreferences.getInt(SPINNER1,0);
        lot2_spinner_status = sharedPreferences.getInt(SPINNER2,0);
        lot3_spinner_status = sharedPreferences.getInt(SPINNER3,0);

    }

    //update previously selected data on launch
    public void updateViews() {

        spinner1.setSelection(lot1_spinner_status);
        spinner2.setSelection(lot2_spinner_status);
        spinner3.setSelection(lot3_spinner_status);

    }

    public void readSharedPreferences() {
        //load in spinner items to a string list
        String[] user_choices = getResources().getStringArray(R.array.user_choices);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        //print to log
        Log.i("PL1 Choice", "value="  + user_choices[sharedPreferences.getInt(SPINNER1, 0)]);
        Log.i("PL2 Choice", "value="  + user_choices[sharedPreferences.getInt(SPINNER2, 0)]);
        Log.i("PL3 Choice", "value="  + user_choices[sharedPreferences.getInt(SPINNER3, 0)]);

    }
}
