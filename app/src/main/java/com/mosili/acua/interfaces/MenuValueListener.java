package com.mosili.acua.interfaces;

import com.mosili.acua.models.WashMenu;

import java.util.List;

/**
 * Created by Ghost on 23/8/2017.
 */

public interface MenuValueListener {
    void onLoadedMenu(List<WashMenu> costList);
}
