package com.acua.app.service;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

/**
 * Created by Ghost on 10/10/2017.
 */

public class NotificationService extends NotificationExtenderService {

    private static final String TAG = "NotificationService";

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        if (receivedResult != null) {

        }
        // Return true to stop the notification from displaying.
        return false;
    }

}
