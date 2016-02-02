package com.gnz.compass.core;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;


public class SensorsListener implements SensorEventListener, LocationListener {

    private SensorsListenerContext context;
    private SensorManager sensorManager;
    private Sensor accelerationSensor;
    private Sensor magneticFieldSensor;
    private LocationManager locationManager;
    private Location currentLocation;
    private Location destLocation;
    private float[] gravityArray;
    private float[] geoMagneticArray;
    private float heading;
    private GeomagneticField geoField;


    public SensorsListener(SensorsListenerContext context) {
        sensorManager = (SensorManager) ((Context) context).getSystemService(Context.SENSOR_SERVICE);
        this.context = context;
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        locationManager = (LocationManager) ((Context) context).getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geoMagneticArray = event.values.clone();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravityArray = event.values.clone();

        if (gravityArray != null && geoMagneticArray != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            if (SensorManager.getRotationMatrix(R, I, gravityArray, geoMagneticArray)) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = orientation[0];
                heading = -azimuth * 360 / (2 * Double.valueOf(Math.PI).floatValue());
                context.onSensorChange();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void startListening() {
        sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_UI);
        startLocationManager();
        context.onSensorChange();
    }

    public void stopListening() {
        sensorManager.unregisterListener(this);
        if (ActivityCompat.checkSelfPermission((Context) context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((Context) context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
    }


    public float getNorth() {
        if (geoField != null)
            return (normalizeDegree(heading + geoField.getDeclination()) + 360) % 360;
        return heading;
    }

    public float getBearingToDestination() {
        if (currentLocation != null && destLocation != null)
            return currentLocation.bearingTo(destLocation) + getNorth();
        return 0f;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    private void setGeoField(Location location) {
        if (location != null)
            geoField = new GeomagneticField(
                    Double.valueOf(location.getLatitude()).floatValue(),
                    Double.valueOf(location.getLongitude()).floatValue(),
                    Double.valueOf(location.getAltitude()).floatValue(),
                    location.getTime()
            );
    }

    public void setDestinationLocation(float lat, float lon) {
        destLocation = new Location("dummy");
        destLocation.setLatitude(lat);
        destLocation.setLongitude(lon);
    }

    private void startLocationManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission((Context) context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((Context) context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        // Request for update every 1 second or 1 meter
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                1000, 1, this);
        setGeoField(currentLocation);
    }

    private float normalizeDegree(float value) {
        if (value >= 0.0f && value <= 180.0f) {
            return value;
        } else {
            return 180 + (180 + value);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        setGeoField(location);
        context.onSensorChange();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
