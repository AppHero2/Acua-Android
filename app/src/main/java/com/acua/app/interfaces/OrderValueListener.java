package com.acua.app.interfaces;

import com.acua.app.models.Order;

import java.util.List;

/**
 * Created by Ghost on 23/8/2017.
 */

public interface OrderValueListener {
    void onLoadedOrder(List<Order> orderList);
}
