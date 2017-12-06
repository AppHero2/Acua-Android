package com.mosili.acua.models;

/**
 * Created by BKing on 12/6/2017.
 */

public class Cost {
    String idx;
    Double price;

    public Cost(String idx, Double price){
        this.idx = idx;
        this.price = price;
    }

    public String getIdx() {
        return idx;
    }

    public double getPrice() {
        return price;
    }
}
