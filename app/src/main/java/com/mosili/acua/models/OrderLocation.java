package com.mosili.acua.models;

/**
 * Created by BKing on 12/6/2017.
 */

public class OrderLocation {
    String name;
    double latitude = 0.0, longitude = 0.0;

    public OrderLocation(String name, double lat, double lng){
        this.name = name;
        this.latitude = lat;
        this.longitude = lng;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
