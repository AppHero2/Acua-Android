package com.mosili.acua.models;

import android.location.Location;

/**
 * Created by BKing on 12/6/2017.
 */

public class Order {
    public String idx;
    public User customer;
    public CarType carType;
    public WashType washType;
    public Cost cost;
    public Location location;
    public long createdAt, updatedAt;
    int status = 0; // 0: pending, 1: delivered, 2: paid, 3: cancelled
}
