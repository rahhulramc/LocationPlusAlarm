package com.example.abhisheikh.locationplusalarm;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by abhisheikh on 14/4/17.
 */

public class Alarm {

    /*
    Set defaults
     */

    private LatLng location;
    private String locationName;
    private String label;
    private int range; // In meters
    private int ringtoneId;
    private boolean active;
    private boolean vibrate;

    public Alarm(LatLng location){
        this.location = location;
    }

    public Alarm(LatLng location, String label, int range, int ringtoneId, boolean active, boolean vibrate) {
        this.location = location;
        this.label = label;
        this.range = range;
        this.ringtoneId = ringtoneId;
        this.active = active;
        this.vibrate = vibrate;
        this.locationName = null;
    }

    public Alarm(LatLng location, String locationName, String label, int range, int ringtoneId, boolean active, boolean vibrate) {
        this.location = location;
        this.locationName = locationName;
        this.label = label;
        this.range = range;
        this.ringtoneId = ringtoneId;
        this.active = active;
        this.vibrate = vibrate;
    }


    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getRingtoneId() {
        return ringtoneId;
    }

    public void setRingtoneId(int ringtoneId) {
        this.ringtoneId = ringtoneId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
