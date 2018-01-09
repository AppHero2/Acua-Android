package com.acua.app.models;

/**
 * Created by BKing on 12/6/2017.
 */

public class WashMenu {
    String idx;
    double price;
    long duration;

    public WashMenu(String idx, double price, long duration){
        this.idx = idx;
        this.price = price;
        this.duration = duration;
    }

    public String getIdx() {
        return idx;
    }

    public double getPrice() {
        return price;
    }

    public long getDuration() {
        return duration;
    }
}
