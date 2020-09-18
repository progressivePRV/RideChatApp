package com.helloworld.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.auth.User;

import java.util.Arrays;

public class AskForARide extends AppCompatActivity {

    private static final String TAG = "okay";
    UserProfile user;
    TextView userName;
    TextInputEditText toLocatioin, fromLocation;
    MaterialButton sendRideRequest;
    TextInputLayout textInputTo,textInputFrom;
    String apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_for_a_ride);

        apiKey = getResources().getString(R.string.google_api_key);

        Toolbar t = findViewById(R.id.toolbar_for_sidebar);
        t.setTitleTextColor(Color.WHITE);
        setSupportActionBar(t);
        setTitle("Ask For a Ride");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = (UserProfile) getIntent().getSerializableExtra("user");
        toLocatioin = findViewById(R.id.to_tiet_main);
        fromLocation = findViewById(R.id.from_tiet_main);
        userName = findViewById(R.id.user_name_in_AskForaRide);
        sendRideRequest = findViewById(R.id.send_ride_request);
        textInputTo = findViewById(R.id.til_for_to_location);
        textInputFrom = findViewById(R.id.til_for_from_location);

        userName.setText(user.firstName+ " " +user.lastName);

        Places.initialize(getApplicationContext(), apiKey);
        PlacesClient placesClient = Places.createClient(this);

        sendRideRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTextInputEditLayoutEmpty(toLocatioin)){
                    textInputTo.setError(getString(R.string.cannot_be_empty));
                }else{
                    textInputTo.setError("");
                    if (isTextInputEditLayoutEmpty(fromLocation)){
                        textInputFrom.setError(getString(R.string.cannot_be_empty));
                    }else{
                        textInputFrom.setError("");
                    }
                }
            }
        });


        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }


            @Override
            public void onError(@NotNull Status status) {
                // TODO: Handle the error.
                Log.d(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    boolean isTextInputEditLayoutEmpty(TextInputEditText t){
        if(t.getText().toString().equals("")){
            return true;
        }else{
            return false;
        }
    }
}