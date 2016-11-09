package com.example.martinhuang.mapgps;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by martinhuang on 11/8/16.
 */

public class Message {

    private String message;
    private String latLng;

    public Message() {

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }
}
