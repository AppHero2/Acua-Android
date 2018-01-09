package com.acua.app.interfaces;

import com.acua.app.models.WashMenu;

import java.util.List;

/**
 * Created by Ghost on 23/8/2017.
 */

public interface MenuValueListener {
    void onLoadedMenu(List<WashMenu> costList);
}
