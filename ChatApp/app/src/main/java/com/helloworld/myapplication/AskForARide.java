package com.helloworld.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.type.LatLng;

import java.util.Arrays;

public class AskForARide extends AppCompatActivity {

    private static final String TAG = "okay";
    UserProfile user;
    TextView userName;
    FirebaseAuth mAuth;
    private FirebaseFirestore db;
    TextInputEditText toLocatioin, fromLocation;
    MaterialButton sendRideRequest;
    TextInputLayout textInputTo,textInputFrom;
    String apiKey;
    private EditText etPlaceFrom;
    private EditText efPlaceTo;
    private com.google.android.gms.maps.model.LatLng fromLatLong;
    private com.google.android.gms.maps.model.LatLng toLatLong;

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
        mAuth=FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sendRideRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(checkValidations(etPlaceFrom) && checkValidations(efPlaceTo)) {
                  RequestedRides requestedRides = new RequestedRides();
                  requestedRides.riderId = mAuth.getUid();
                  requestedRides.dropOffLocation = toLatLong;
                  requestedRides.pickUpLocation = fromLatLong;
                  requestedRides.rideStatus = "REQUESTED";

                          db.collection("ChatRoomList")
                                  .document(getIntent().getExtras().getString("chatRoomName"))
                                  .collection("Requested Rides")
                                  .document(mAuth.getUid())
                                  .set(requestedRides)
                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {
                                          Toast.makeText(AskForARide.this, "Lets see if it stores without any problem!", Toast.LENGTH_SHORT).show();
                                      }
                                  }).addOnFailureListener(new OnFailureListener() {
                              @Override
                              public void onFailure(@NonNull Exception e) {
                                  Toast.makeText(AskForARide.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                              }
                          });
              }
            }
        });

        String apiKey = getString(R.string.api_key);

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment_from = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_from);

        etPlaceFrom = (EditText) autocompleteFragment_from.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlaceFrom.setHint("Enter From Location");

        

        autocompleteFragment_from.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment_from.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                fromLatLong = place.getLatLng();
                Toast.makeText(AskForARide.this, fromLatLong+"", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment_to = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_to);

        efPlaceTo = (EditText) autocompleteFragment_to.getView().findViewById(R.id.places_autocomplete_search_input);
        efPlaceTo.setHint("Enter To Location");

        autocompleteFragment_to.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment_to.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                toLatLong = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

//    boolean isTextInputEditLayoutEmpty(TextInputEditText t){
//        if(t.getText().toString().equals("")){
//            return true;
//        }else{
//            return false;
//        }
//    }


    //For checking the empty strings
    public boolean checkValidations(EditText editText){
        if(editText.getText().toString().trim().equals("")){
            editText.setError("Cannot be empty");
            return false;
        }else{
            return true;
        }
    }
}