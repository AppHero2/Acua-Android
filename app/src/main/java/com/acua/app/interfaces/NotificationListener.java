package com.acua.app.interfaces;

import com.acua.app.models.Notification;

/**
 * Created by BKing on 1/15/2018.
 */

public interface NotificationListener {
    void onReceivedNotification(Notification notification);
    void onRemovedNotification(Notification notification);
}
