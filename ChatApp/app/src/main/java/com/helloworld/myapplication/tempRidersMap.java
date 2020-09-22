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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.core.Query;

public class tempRidersMap extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "okay";
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private String chatRoomName;
    private UserProfile userProfile;
    private RequestedRides request;
    FusedLocationProviderClient fusedLocationProviderClient;
    ListenerRegistration registration;
    Marker driverMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_riders_map);

        db = FirebaseFirestore.getInstance();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        chatRoomName ="r6";

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
                    Toast.makeText(tempRidersMap.this, "got data from r6 chatroom", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: got request ridr object=>"+request);
                    SetDetailsOnMap();
                }
            }
        });
    }

    void SetDetailsOnMap(){

        LatLngBounds.Builder latlngBuilder =  new LatLngBounds.Builder();
        //adding markers and locations
        //addinf from location
        LatLng fromLatLong = new LatLng(request.pickUpLocation.get(0), request.pickUpLocation.get(1));
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
        Log.d(TAG, "SetDetailsOnMap: driver lat"+driverLatlng.latitude);
        Log.d(TAG, "SetDetailsOnMap: driver lng"+driverLatlng.longitude);
        driverMarker = mMap.addMarker(new MarkerOptions()
                .position(driverLatlng)
                .title("Driver")
                .icon(bitmapDescriptorFromVector(R.drawable.ic_baseline_directions_car_24)));

        final LatLngBounds latLngBounds = latlngBuilder.build();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,200));
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,200));
                SetAnimatoinForDriver();
                GetDriverLocations();
            }
        });
    }
    
    void SetAnimatoinForDriver(){
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(driverMarker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 20000;

        final Interpolator interpolator = new LinearInterpolator();

      //  handler.post(new Runnable() {
      //      @Override
      //      public void run() {
      //          long elapsed = SystemClock.uptimeMillis() - start;
      //          float t = interpolator.getInterpolation((float) elapsed
      //                  / duration);
      //          double lng = t * toPosition.longitude + (1 - t)
      //                   * startLatLng.longitude;
                double lng = request.driverLocation.get(1);
      //          double lat = t * toPosition.latitude + (1 - t)
      //                  * startLatLng.latitude;
                double lat = request.driverLocation.get(0);
                driverMarker.setPosition(new LatLng(lat, lng));
                //driverLatLngArrList.clear();
                //driverLatLngArrList.add(lat);
                //driverLatLngArrList.add(lng);
                //UpdateDriverLocation();
      //          if (t < 1.0) {
                    // Post again 16ms later.
      //              handler.postDelayed(this, 500);
      //          } else {
      //              if (hideMarker) {
      //                  marker.setVisible(false);
      //              } else {
      //                  marker.setVisible(true);
      //              }
      //          }
      //      }
      //  });
    }
    
    void UpdatemarkerLocation(){
        double lng = request.driverLocation.get(1);
        double lat = request.driverLocation.get(0);
        driverMarker.setPosition(new LatLng(lat, lng));
    }

    void GetDriverLocations(){
        DocumentReference query = db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .document("jBWHYtvax5RGTvPmDM1I0VXdjsx2");

        registration = query.addSnapshotListener(tempRidersMap.this,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }else{
                    if(value != null){
                        request = value.toObject(RequestedRides.class);
                        if(checkRideStatus()){
                            Log.d(TAG, "onEvent: Driver got to the location");
                            Toast.makeText(tempRidersMap.this, "Rider meets driver", Toast.LENGTH_SHORT).show();
                            registration.remove();
                        }else{
                            Log.d(TAG, "onEvent: callling update marker location from getDriverLocation");
                            UpdatemarkerLocation();   
                        }
                    }else{
                        Log.d(TAG, "onEvent: got nothing in getdriverLocation");
                    }
                }
            }
        });
    }

    private boolean checkRideStatus() {
        if(request.rideStatus.equals("SUCCESS")){
            return true;
        }
        return false;
    }

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
}