package com.helloworld.myapplication;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class RequestedRides implements Serializable {
    String riderId;
    String riderName;
    String driverId;
    String driverName;
    HashMap<String, UserProfile> drivers = new HashMap<>();
    ArrayList<Double> pickUpLocation = new ArrayList<>();
    ArrayList<Double> dropOffLocation = new ArrayList<>();
    String rideStatus;

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public String getRiderName() {
        return riderName;
    }

    public void setRiderName(String riderName) {
        this.riderName = riderName;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public HashMap<String, UserProfile> getDrivers() {
        return drivers;
    }

    public void setDrivers(HashMap<String, UserProfile> drivers) {
        this.drivers = drivers;
    }

    public ArrayList<Double> getPickUpLocation() {
        return pickUpLocation;
    }

    public void setPickUpLocation(ArrayList<Double> pickUpLocation) {
        this.pickUpLocation = pickUpLocation;
    }

    public ArrayList<Double> getDropOffLocation() {
        return dropOffLocation;
    }

    public void setDropOffLocation(ArrayList<Double> dropOffLocation) {
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
                ", riderName='" + riderName + '\'' +
                ", driverId='" + driverId + '\'' +
                ", driverName='" + driverName + '\'' +
                ", drivers=" + drivers +
                ", pickUpLocation=" + pickUpLocation +
                ", dropOffLocation=" + dropOffLocation +
                ", rideStatus='" + rideStatus + '\'' +
                '}';
    }
}
