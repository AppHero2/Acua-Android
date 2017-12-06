package com.mosili.acua.interfaces;

import com.mosili.acua.models.Cost;

import java.util.List;

/**
 * Created by Ghost on 23/8/2017.
 */

public interface CostValueListener {
    void onLoadedCosts(List<Cost> costList);
}
