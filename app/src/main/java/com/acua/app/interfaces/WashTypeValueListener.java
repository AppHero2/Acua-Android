package com.acua.app.interfaces;

import com.acua.app.models.WashType;

import java.util.List;

/**
 * Created by Ghost on 23/8/2017.
 */

public interface WashTypeValueListener {
    void onLoadedWashTypes(List<WashType> washTypes);
}
