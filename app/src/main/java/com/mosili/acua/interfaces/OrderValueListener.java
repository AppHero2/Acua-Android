package com.mosili.acua.interfaces;

import com.mosili.acua.models.Order;

import java.util.List;

/**
 * Created by Ghost on 23/8/2017.
 */

public interface OrderValueListener {
    void onLoadedOrder(List<Order> orderList);
}
