package com.helloworld.myapplication;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class tempDriverMap extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "okay";
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private String chatRoomName;
    private UserProfile userProfile;
    private RequestedRides request;
    FusedLocationProviderClient fusedLocationProviderClient;
    TextView riderName;
    TextView toLocation;
    TextView fromLocation;
//    Double diffX,diffY;
    Handler handler = new Handler();
//    int counter=0;
    MarkerOptions driverMarkerOption;
    Marker driverMarker;
    ArrayList<Double> driverLatLngArrList =  new ArrayList<>();
//    Double nlat,nlng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_driver_map);
        Log.d(TAG, "onCreate: oncreate in tempDriverMap called");

        riderName = findViewById(R.id.riderName);
        toLocation = findViewById(R.id.toLocation);
        fromLocation = findViewById(R.id.fromLocation);

        db = FirebaseFirestore.getInstance();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.driverMap);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        chatRoomName ="r6";

        //get requestRides objects from r6
        GetRequestRideDetails();

    }

    void GetRequestRideDetails(){
        db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .document("jBWHYtvax5RGTvPmDM1I0VXdjsx2")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    request = document.toObject(RequestedRides.class);
                    Toast.makeText(tempDriverMap.this, "got data from r6 chatroom", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: got request ridr object=>"+request);
                    //GetDistanceBTWDriverAndRider();
                    SetDetailsInActivity();
                }
            }
        });
    }

    void SetDetailsInActivity(){
        riderName.setText(request.riderName);
        toLocation.setText(request.toLocation);
        fromLocation.setText(request.fromLocation);
        SetDetailsOnMap();
    }

    void SetDetailsOnMap(){

        LatLngBounds.Builder latlngBuilder =  new LatLngBounds.Builder();
        //adding markers and locations
        //addinf from location
        final LatLng fromLatLong = new LatLng(request.pickUpLocation.get(0), request.pickUpLocation.get(1));
        latlngBuilder.include(fromLatLong);
        MarkerOptions marker = new MarkerOptions()
                .position(fromLatLong)
                .title("Pick Up Location");
        marker.icon(BitmapDescriptorFactory.defaultMarker(120));
        mMap.addMarker(marker);
        //addingdrop location
        LatLng toLatLng = new LatLng(request.dropOffLocation.get(0), request.dropOffLocation.get(1));
        latlngBuilder.include(toLatLng);
        mMap.addMarker(new MarkerOptions()
                .position(toLatLng)
                .title("Drop Location"));
        //adding driver marker
        LatLng driverLatlng = new LatLng(request.driverLocation.get(0), request.driverLocation.get(1));
        latlngBuilder.include(driverLatlng);
        driverMarkerOption = new MarkerOptions()
                .position(driverLatlng)
                .title("Driver")
                .icon(bitmapDescriptorFromVector(R.drawable.ic_baseline_directions_car_24));
        driverMarker = mMap.addMarker(driverMarkerOption);

        final LatLngBounds latLngBounds = latlngBuilder.build();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,200));
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,200));
                tryAnimateMarker(driverMarker,fromLatLong,true);
            }
        });


        //startDriverAnimate();
    }

    void tryAnimateMarker(final Marker marker, final LatLng toPosition,final boolean hideMarker){

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 20000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                driverLatLngArrList.clear();
                driverLatLngArrList.add(lat);
                driverLatLngArrList.add(lng);
                UpdateDriverLocation();
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 500);
                } else {
                    UpadteRideStatus();
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    //driver location
    // 0 : 35.3093032
    // 1 : -80.7217352

    void UpdateDriverLocation(){
        DocumentReference rideRequeat = db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .document("jBWHYtvax5RGTvPmDM1I0VXdjsx2");

        rideRequeat.update("driverLocation",driverLatLngArrList)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: updated driver location in firebase");
                        }else{
                            Log.d(TAG, "onComplete: error while updating driver location in friebase");
                        }
                    }
                });
    }

    void UpadteRideStatus(){
        DocumentReference rideRequeat = db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .document("jBWHYtvax5RGTvPmDM1I0VXdjsx2");

        rideRequeat.update("rideStatus","SUCCESS")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: updated ride Status in firebase");
                        }else{
                            Log.d(TAG, "onComplete: error while updating ride Status in friebase");
                        }
                    }
                });
    }

//    void startDriverAnimate(){
//        Projection proj = mMap.getProjection();
//        Point startPoint = proj.toScreenLocation(driverMarker.getPosition());
//        LatLng startLatLng = proj.fromScreenLocation(startPoint);
//        callHandler();
//    }
//
//    void callHandler(){
//        handler.post(new Runnable() {
//            public void run() {
//                Log.d(TAG, "run: in call handler with counter"+counter);
//                driverMarker.setPosition(new LatLng(nlat+diffX,nlng+diffY));
//                if (counter>9){
//                    //do nothing
//                }else{
//                    counter++;
//                    handler.postDelayed(this,2000);
//                }
//            }
//        });
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

    }

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

//    void GetDistanceBTWDriverAndRider(){
//        Double riderX,riderY,DriverX,DriverY;
//        riderX = request.pickUpLocation.get(0);
//        riderY = request.pickUpLocation.get(1);
//        DriverX = request.driverLocation.get(0);
//        DriverY = request.driverLocation.get(1);
//        diffX = riderX-DriverX;
//        diffY = riderY-DriverY;
//        //break it into 10
//        diffX/=10;
//        diffY/=10;
//        nlat = DriverX;
//        nlng = DriverY;
//    }



}