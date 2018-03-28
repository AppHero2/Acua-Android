package com.acua.app.models;

import java.util.Map;

import static com.acua.app.utils.Util.getBooleanFromData;
import static com.acua.app.utils.Util.getIntFromData;
import static com.acua.app.utils.Util.getLongFromData;
import static com.acua.app.utils.Util.getStringFromData;

/**
 * Created by BKing on 1/15/2018.
 */

public class Notification {

    private String idx, title, message;
    private long createdAt;
    private boolean isRead;

    public Notification(Map<String, Object> data){
        updateData(data);
    }

    public void updateData(Map<String, Object> data){
        this.idx = getStringFromData("idx", data);
        this.title = getStringFromData("title", data);
        this.message = getStringFromData("message", data);
        this.createdAt = getLongFromData("createdAt", data);
        this.isRead = getBooleanFromData("isRead", data);
    }

    public String getIdx() {
        return idx;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return isRead;
    }
}
