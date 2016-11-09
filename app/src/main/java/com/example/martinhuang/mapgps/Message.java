package com.example.martinhuang.mapgps;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by martinhuang on 11/8/16.
 */

public class Message {

    private String message;
    private String latitude;
    private String longitude;

    public Message() {

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
