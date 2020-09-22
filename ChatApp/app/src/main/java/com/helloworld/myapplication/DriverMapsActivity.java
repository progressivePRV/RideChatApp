package com.helloworld.myapplication;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Context;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firestore.v1.WriteResult;

//import com.google.maps.DirectionsApi;
//import com.google.maps.DirectionsApiRequest;
//import com.google.maps.GeoApiContext;
//import com.google.maps.model.DirectionsLeg;
//import com.google.maps.model.DirectionsResult;
//import com.google.maps.model.DirectionsRoute;
//import com.google.maps.model.DirectionsStep;
//import com.google.maps.model.EncodedPolyline;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

import static android.provider.SettingsSlicesContract.KEY_LOCATION;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "okay";
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private CameraPosition cameraPosition;
    private static final int DEFAULT_ZOOM = 15;
    private ProgressDialog progressDialog;
    private String chatRoomName;
    private UserProfile userProfile;
    private RequestedRides updateRides;
    boolean isYesClicked;
    TextView riderName;
    TextView toLocation;
    TextView fromLocation;
    ImageView imageViewRider;
    ImageView imageViewpickUpLocation;
    ImageView imageViewDropOffLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_driver_maps);

        imageViewRider = findViewById(R.id.imageViewRider);
        imageViewpickUpLocation = findViewById(R.id.imageViewstartLocation);
        imageViewDropOffLocation = findViewById(R.id.imageViewdropOffLocation);

        imageViewRider.setImageResource(R.drawable.profileinfouser);
        imageViewpickUpLocation.setImageResource(R.drawable.rec);
        imageViewDropOffLocation.setImageResource(R.drawable.placeholder);

        db = FirebaseFirestore.getInstance();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.driverMap);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        chatRoomName = getIntent().getExtras().getString("chatRoomName");
        userProfile = (UserProfile) getIntent().getExtras().getSerializable("userProfile");

        isYesClicked=false;

        final RequestedRides requestedRides = (RequestedRides) getIntent().getExtras().getSerializable("requestedRides");
        riderName=findViewById(R.id.riderName);
        toLocation=findViewById(R.id.toLocation);
        fromLocation=findViewById(R.id.fromLocation);

        riderName.setText(requestedRides.riderName);
        toLocation.setText(requestedRides.toLocation);
        fromLocation.setText(requestedRides.fromLocation);

        // Prompt the user for permission.
        getLocationPermission();
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        //if the driver says yes
        findViewById(R.id.buttonDriverYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYesClicked=true;
                Toast.makeText(DriverMapsActivity.this, "Sending confirmation. Please wait", Toast.LENGTH_SHORT).show();

                progressDialog = new ProgressDialog(DriverMapsActivity.this);
                progressDialog.setMessage("Fetching your ride details");
                progressDialog.setCancelable(false);
                progressDialog.show();

                //Adding this driver to the driver list

                requestedRides.drivers.add(userProfile);
                requestedRides.rideStatus = "IN_PROGRESS";


                DocumentReference driverReference =  db.collection("ChatRoomList")
                        .document(chatRoomName)
                        .collection("Requested Rides")
                        .document(requestedRides.riderId);

// Atomically add a new region to the "regions" array field.
                       driverReference.update("drivers",
                        FieldValue.arrayUnion(userProfile))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //showProgressBarDialogWithHandler();

                                //Toast.makeText(DriverMapsActivity.this, "Lets see if it stores without any problem!", Toast.LENGTH_SHORT).show();

                                final DocumentReference docRefDriver = db.collection("ChatRoomList")
                                        .document(chatRoomName)
                                        .collection("Requested Rides")
                                        .document(requestedRides.riderId);

                                docRefDriver.addSnapshotListener(DriverMapsActivity.this,new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                                        if (error != null) {
                                            progressDialog.dismiss();
                                            Log.d("demo:", error+"");
                                            return;
                                        }

                                        if (snapshot != null && snapshot.exists()) {
                                            updateRides = snapshot.toObject(RequestedRides.class);
                                            //showProgressBarDialogWithHandler();
                                            if(updateRides.rideStatus.equals("ACCEPTED")){
                                                Log.d(TAG, "onEvent: ride is Accepted");
                                                if(updateRides.driverId.equals(userProfile.uid)){
                                                    getLocationPermission();
                                                    try {
                                                       // if (locationPermissionGranted) {
                                                            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                                                            locationResult.addOnCompleteListener(DriverMapsActivity.this, new OnCompleteListener<Location>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Location> task) {
                                                                    if (task.isSuccessful()) {
                                                                        // Set the map's camera position to the current location of the device.
                                                                        lastKnownLocation = task.getResult();
                                                                        if (lastKnownLocation != null) {
                                                                            Log.d(TAG, "onComplete: last known location is not null");
                                                                            ArrayList<Double> driverLocation = new ArrayList<>();
                                                                            driverLocation.add(lastKnownLocation.getLatitude());
                                                                            driverLocation.add(lastKnownLocation.getLongitude());

                                                                            updateRides.setDriverLocation(driverLocation);
                                                                            //updateRides.setDrivers(null);

                                                                            docRefDriver.set(updateRides).addOnCompleteListener(DriverMapsActivity.this, new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        progressDialog.dismiss();
                                                                                        Toast.makeText(DriverMapsActivity.this, "You have been selected for this ride.. Wait the intent will come soon", Toast.LENGTH_SHORT).show();
                                                                                        Intent intent= new Intent(DriverMapsActivity.this,OnRideActivity.class);
                                                                                        intent.putExtra("requestedRide",updateRides);
                                                                                        intent.putExtra("chatRoomName",chatRoomName);
                                                                                        //intent.putExtra("driverLatitude",lastKnownLocation.getLatitude());
                                                                                        //intent.putExtra("driverLongitude",lastKnownLocation.getLongitude());
                                                                                        startActivity(intent);
                                                                                        finish();
                                                                                    }
                                                                                    else{
                                                                                        progressDialog.dismiss();
                                                                                        Toast.makeText(DriverMapsActivity.this, "There was some problem with the ride. Ride cancled", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });

                                                                        }
                                                                    } else {
                                                                        progressDialog.dismiss();
                                                                        Log.d("demo", "Current location is null. Using defaults.");
                                                                        Log.e("demo", "Exception: %s", task.getException());
                                                                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                                                                    }
                                                                }
                                                            });
//                                                        }
//                                                        else {
//                                                            progressDialog.dismiss();
//                                                            Toast.makeText(DriverMapsActivity.this, "No permission to access maps", Toast.LENGTH_SHORT).show();
//                                                            finish();
//                                                        }
                                                    } catch (SecurityException e) {
                                                        progressDialog.dismiss();
                                                        Log.e("Exception: %s", e.getMessage(), e);
                                                    }
                                                }else{
                                                    progressDialog.dismiss();
                                                    Toast.makeText(DriverMapsActivity.this, "Sorry, the ride is not available", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            }


                                        } else {
                                            System.out.print("Current data: null");
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
                        Toast.makeText(DriverMapsActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isYesClicked){
                    Toast.makeText(DriverMapsActivity.this, "Ride rejected", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        },30000);

        findViewById(R.id.buttonDriverNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isYesClicked){
                    Toast.makeText(DriverMapsActivity.this, "Ride rejected", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        //Adding listener if we see that the driver is picked
        //setting snapshot listener to the drivers accepted list and adding it in a list to display it in the alert box
//        DocumentReference docRefDriver = db.collection("ChatRoomList")
//                .document(chatRoomName)
//                .collection("Requested Rides")
//                .document(userProfile.uid);
//
//        docRefDriver.addSnapshotListener(DriverMapsActivity.this, new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Log.d("demo:", error+"");
//                    return;
//                }
//
//                if (snapshot != null && snapshot.exists()) {
//                    updateRides = snapshot.toObject(RequestedRides.class);
//
//                } else {
//                    System.out.print("Current data: null");
//                }
//            }
//        });
    }

    //for showing the progress dialog
    public void showProgressBarDialogWithHandler()
    {
//        progressDialog = new ProgressDialog(DriverMapsActivity.this);
//        progressDialog.setMessage("Fetching your ride details");
//        progressDialog.setCancelable(false);
//        progressDialog.show();

        //Handler is set for 30 seconds for the driver to accept the invitation
        //Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                progressDialog.dismiss();
                if(updateRides.rideStatus.equals("ACCEPTED")){
                    if(updateRides.driverId.equals(userProfile.uid)){
                        Toast.makeText(DriverMapsActivity.this, "You have been selected for this ride.. Wait the intent will come soon", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(DriverMapsActivity.this, "Sorry, the ride is not available", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
//            }
//        }, 40000);
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {


                                MarkerOptions marker = new MarkerOptions()
                                        .position( new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()))
                                        .title("Your Location");
                                marker.icon(bitmapDescriptorFromVector(R.drawable.car));
                                mMap.addMarker(marker);
                            }
                        } else {
                            Log.d("demo", "Current location is null. Using defaults.");
                            Log.e("demo", "Exception: %s", task.getException());
//                            mMap.moveCamera(CameraUpdateFactory
//                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

//For changing the vector asset to bitmap
    private BitmapDescriptor bitmapDescriptorFromVector(@DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_directions_car_24);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor bitmapDescriptorFromVectorPerson(@DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_person_pin_circle_24);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
        /**
         * Updates the map's UI settings based on whether the user has granted location permission.
         */
        // [START maps_current_place_update_location_ui]
        private void updateLocationUI() {
            if (mMap == null) {
                return;
            }
            try {
                if (locationPermissionGranted) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                } else {
                    mMap.setMyLocationEnabled(false);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    lastKnownLocation = null;
                    getLocationPermission();
                }
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
        }
        // [END maps_current_place_update_location_ui]


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLngBounds.Builder latlngBuilder =  new LatLngBounds.Builder();

//        PolylineOptions polylineOptions =  new PolylineOptions();

        RequestedRides requestedRides = (RequestedRides) getIntent().getExtras().getSerializable("requestedRides");
        LatLng fromLatLong = new LatLng(requestedRides.pickUpLocation.get(0), requestedRides.pickUpLocation.get(1));
        latlngBuilder.include(fromLatLong);
        MarkerOptions marker = new MarkerOptions()
                .position(fromLatLong)
                .title("Pick Up Location");
        marker.icon(bitmapDescriptorFromVectorPerson(R.drawable.ic_baseline_person_pin_circle_24));
        mMap.addMarker(marker);

        LatLng toLatLng = new LatLng(requestedRides.dropOffLocation.get(0), requestedRides.dropOffLocation.get(1));
        latlngBuilder.include(toLatLng);
        mMap.addMarker(new MarkerOptions()
                .position(toLatLng)
                .title("Drop Location"));

//        Polyline polyline = mMap.addPolyline(polylineOptions);

        final LatLngBounds latLngBounds = latlngBuilder.build();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,200));
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,200));
            }
        });


        //Define list to get all latlng for the route

//        List<LatLng> path = new ArrayList();
//        //Execute Directions API request
//        GeoApiContext context = new GeoApiContext.Builder()
//                .apiKey("AIzaSyBuIvBN797lPyHRIASQJzk77k0ry")
//                .build();
//        DirectionsApiRequest req = DirectionsApi.getDirections(context, String.valueOf(fromLatLong), String.valueOf(toLatLng));
//        try {
//            DirectionsResult res = req.await();
//
//            //Loop through legs and steps to get encoded polylines of each step
//            if (res.routes != null && res.routes.length > 0) {
//                DirectionsRoute route = res.routes[0];
//
//                if (route.legs !=null) {
//                    for(int i=0; i<route.legs.length; i++) {
//                        DirectionsLeg leg = route.legs[i];
//                        if (leg.steps != null) {
//                            for (int j=0; j<leg.steps.length;j++){
//                                DirectionsStep step = leg.steps[j];
//                                if (step.steps != null && step.steps.length >0) {
//                                    for (int k=0; k<step.steps.length;k++){
//                                        DirectionsStep step1 = step.steps[k];
//                                        EncodedPolyline points1 = step1.polyline;
//                                        if (points1 != null) {
//                                            //Decode polyline and add points to list of route coordinates
//                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
//                                            for (com.google.maps.model.LatLng coord1 : coords1) {
//                                                path.add(new LatLng(coord1.lat, coord1.lng));
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    EncodedPolyline points = step.polyline;
//                                    if (points != null) {
//                                        //Decode polyline and add points to list of route coordinates
//                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
//                                        for (com.google.maps.model.LatLng coord : coords) {
//                                            path.add(new LatLng(coord.lat, coord.lng));
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch(Exception ex) {
//            Log.e("demo", ex.getLocalizedMessage());
//        }
//
//        //Draw the polyline
//        if (path.size() > 0) {
//            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
//            mMap.addPolyline(opts);
//        }
//
//        mMap.getUiSettings().setZoomControlsEnabled(true);

//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(, 6));

    }
}