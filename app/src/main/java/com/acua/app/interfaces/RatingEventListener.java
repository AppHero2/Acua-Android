package com.acua.app.interfaces;

import com.acua.app.models.Notification;

/**
 * Created by Ghost on 23/8/2017.
 */

public interface RatingEventListener {
    void onRatingEventRequired(Notification notification);
}
