package com.helloworld.myapplication;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class RequestedRides {
    String riderId;
    String driverId;
    ArrayList<String> drivers = new ArrayList<>();
    com.google.android.gms.maps.model.LatLng pickUpLocation;
    com.google.android.gms.maps.model.LatLng dropOffLocation;
    String rideStatus;

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public ArrayList<String> getDrivers() {
        return drivers;
    }

    public void setDrivers(ArrayList<String> drivers) {
        this.drivers = drivers;
    }

    public LatLng getPickUpLocation() {
        return pickUpLocation;
    }

    public void setPickUpLocation(LatLng pickUpLocation) {
        this.pickUpLocation = pickUpLocation;
    }

    public LatLng getDropOffLocation() {
        return dropOffLocation;
    }

    public void setDropOffLocation(LatLng dropOffLocation) {
        this.dropOffLocation = dropOffLocation;
    }

    public String getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(String rideStatus) {
        this.rideStatus = rideStatus;
    }

    @Override
    public String toString() {
        return "RequestedRides{" +
                "riderId='" + riderId + '\'' +
                ", driverId='" + driverId + '\'' +
                ", drivers=" + drivers +
                ", pickUpLocation=" + pickUpLocation +
                ", dropOffLocation=" + dropOffLocation +
                ", rideStatus='" + rideStatus + '\'' +
                '}';
    }
}
