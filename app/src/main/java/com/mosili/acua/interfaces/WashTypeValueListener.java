package com.mosili.acua.interfaces;

import com.mosili.acua.models.WashType;

import java.util.List;

/**
 * Created by Ghost on 23/8/2017.
 */

public interface WashTypeValueListener {
    void onLoadedWashTypes(List<WashType> washTypes);
}
