package com.acua.app.models;

/**
 * Created by BKing on 12/6/2017.
 */

public class CarType {
    String idx, name;

    public CarType(String idx, String name){
        this.idx = idx;
        this.name = name;
    }

    public String getIdx() {
        return idx;
    }

    public String getName() {
        return name;
    }
}
