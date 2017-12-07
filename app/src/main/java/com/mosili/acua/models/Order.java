package com.mosili.acua.models;


import java.util.Map;

import static com.mosili.acua.models.OrderPayStatus.UNPAID;
import static com.mosili.acua.models.OrderServiceStatus.PENDING;
import static com.mosili.acua.utils.Util.getDateFromData;
import static com.mosili.acua.utils.Util.getIntFromData;
import static com.mosili.acua.utils.Util.getMapDataFromData;
import static com.mosili.acua.utils.Util.getStringFromData;

/**
 * Created by BKing on 12/6/2017.
 */

public class Order {
    public String idx;
    public String customerId;
    public WashMenu menu;
    public OrderLocation location;
    public long beginAt, endAt;
    public OrderServiceStatus serviceStatus = PENDING;
    public OrderPayStatus payStatus = UNPAID;

    public Order(Map<String, Object> data){
        this.updateData(data);
    }

    public void updateData(Map<String, Object> data){
        this.idx = getStringFromData("uid", data);
        this.customerId = getStringFromData("customerId", data);

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
    }
}
