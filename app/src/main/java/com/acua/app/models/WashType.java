package com.acua.app.models;

/**
 * Created by BKing on 12/6/2017.
 */

public class WashType {
    String idx, name;

    public WashType(String idx, String name){
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
