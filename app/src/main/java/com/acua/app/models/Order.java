package com.acua.app.models;


import com.acua.app.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.acua.app.models.OrderPayStatus.UNPAID;
import static com.acua.app.models.OrderServiceStatus.BOOKED;
import static com.acua.app.utils.Util.getBooleanFromData;
import static com.acua.app.utils.Util.getMapDataFromData;
import static com.acua.app.utils.Util.getStringFromData;

/**
 * Created by BKing on 12/6/2017.
 */

public class Order {
    public String idx;
    public String customerId, customerPushToken;
    public List<String> washers = new ArrayList<>();
    public WashMenu menu;
    public OrderLocation location;
    public long beginAt, endAt, completedAt;
    public OrderServiceStatus serviceStatus = BOOKED;
    public OrderPayStatus payStatus = UNPAID;
    public boolean hasTap = true;
    public boolean hasPlug = true;
    public boolean is24reminded = false;

    public Order() {

    }

    public Order(Map<String, Object> data){
        this.updateData(data);
    }

    public void updateData(Map<String, Object> data){
        this.idx = getStringFromData("idx", data);
        this.customerId = getStringFromData("customerId", data);
        this.customerPushToken = getStringFromData("customerPushToken", data);

        Map<String, Object> menuData = getMapDataFromData("menu", data);
        String menuId = (String) menuData.get("idx");
        Number price = (Number) menuData.get("price");
        Number duration = (Number) menuData.get("duration");
        this.menu = new WashMenu(menuId, price.doubleValue(), duration.longValue());

        Map<String, Object> locationData = getMapDataFromData("location", data);
        String name = (String) locationData.get("name");
        Number latitude = (Number) locationData.get("latitude");
        Number longitude = (Number) locationData.get("longitude");
        this.location = new OrderLocation(name, latitude.doubleValue(), longitude.doubleValue());

        this.payStatus = OrderPayStatus.valueOf(String.valueOf(data.get("payStatus")));
        this.serviceStatus = OrderServiceStatus.valueOf(String.valueOf(data.get("serviceStatus")));

        this.beginAt = Util.getLongFromData("beginAt", data);
        this.endAt = Util.getLongFromData("endAt", data);
        this.completedAt = Util.getLongFromData("completedAt", data);

        this.hasTap = getBooleanFromData("hasTap", data);
        this.hasPlug = getBooleanFromData("hasPlug", data);
        this.is24reminded = getBooleanFromData("is24reminded", data);
    }
}
