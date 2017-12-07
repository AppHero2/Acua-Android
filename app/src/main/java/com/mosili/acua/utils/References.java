package com.mosili.acua.utils;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Ghost on 12/4/2017.
 */

public class References {
    private static References instance;
    private FirebaseDatabase database;
    private Context context;

    public DatabaseReference usersRef, carTypeRef, washTypeRef, costsRef, ordersRef;

    public static void init(Context context, FirebaseDatabase database) {
        instance = new References(context, database);
    }

    public static References getInstance() {
        return instance;
    }

    private References(Context context, FirebaseDatabase database) {
        this.context = context;
        this.database = database;

        usersRef = database.getReference(Constant.USER);
        carTypeRef = database.getReference(Constant.CARS);
        washTypeRef = database.getReference(Constant.WASH);
        costsRef = database.getReference(Constant.COST);
        ordersRef = database.getReference(Constant.ORDER);
    }

    private class Constant {

        public static final String USER = "Users";
        public static final String CARS = "CarType";
        public static final String WASH = "WashType";
        public static final String COST = "Costs";
        public static final String ORDER = "Orders";
    }
}


