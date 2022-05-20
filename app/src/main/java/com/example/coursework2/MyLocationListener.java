package com.example.coursework2;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class MyLocationListener implements LocationListener {
    public Location currentLocation;
    public Float distance;
    public final static String TAG = "mobile";

    MyLocationListener(Location location){
        currentLocation = location;
        distance = (float) 0;
    }

    public void setCurrentLocation(Location location){
        currentLocation = location;
    }

    public Location getCurrentLocation(){
        return currentLocation;
    }

    public Float getDistanceTravelled(){
        return distance;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(currentLocation == null){
            Log.d(TAG, "Initiate location");
            currentLocation = location;
        }else{
            distance = location.distanceTo(currentLocation);
            setCurrentLocation(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // information about the signal, i.e. number of satellites
        Log.d("mobile", "onStatusChanged: " + provider + " " + status);
    }
    @Override
    public void onProviderEnabled(String provider) {
        // the user enabled (for example) the GPS
        Log.d("mobile", "onProviderEnabled: " + provider);
    }
    @Override
    public void onProviderDisabled(String provider) {
        // the user disabled (for example) the GPS
        Log.d("mobile", "onProviderDisabled: " + provider);
    }
}