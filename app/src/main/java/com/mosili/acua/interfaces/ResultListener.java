package com.mosili.acua.interfaces;

import com.mosili.acua.models.CarType;

import java.util.List;

/**
 * Created by Ghost on 23/8/2017.
 */

public interface ResultListener {
    void onResponse(boolean success, String response);
}
