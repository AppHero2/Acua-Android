package com.acua.app.models;

import android.content.Context;

import com.acua.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.acua.app.utils.Util.getIntFromData;
import static com.acua.app.utils.Util.getLongFromData;
import static com.acua.app.utils.Util.getStringFromData;

/**
 * Created by BKing on 1/15/2018.
 */

public class Feedback {

    private String idx, orderID, washerID, senderID, content;
    private int type = 0;
    private long createdAt;

    public Feedback(Map<String, Object> data){
        updateData(data);
    }

    public void updateData(Map<String, Object> data){
        this.idx = getStringFromData("idx", data);
        this.orderID = getStringFromData("orderID", data);
        this.senderID = getStringFromData("senderID", data);
        this.washerID = getStringFromData("washerID", data);
        this.content = getStringFromData("content", data);
        this.type = getIntFromData("type", data);
        this.createdAt = getLongFromData("createdAt", data);
    }

    public String getIdx() {
        return idx;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getWasherID() {
        return washerID;
    }

    public String getContent() {
        return content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getType() {
        return type;
    }

    static public String getFeedbackContent(Context context, int type) {
        String message;
        switch (type) {
            case 1:
                message = context.getString(R.string.feedback_content_1);
                break;
            case 2:
                message = context.getString(R.string.feedback_content_2);
                break;
            case 3:
                message = context.getString(R.string.feedback_content_3);
                break;
            case 4:
                message = context.getString(R.string.feedback_content_4);
                break;
            case 5:
                message = context.getString(R.string.feedback_content_5);
                break;
            case 6:
                message = context.getString(R.string.feedback_content_6);
                break;
            default:
                message = context.getString(R.string.feedback_content_1);
                break;
        }

        return message;
    }

    static public String getFeedbackTitle(Context context, int type) {
        String feedbackTitle;
        switch (type) {
            case 1:
                feedbackTitle = context.getString(R.string.feedback_title_1);
                break;
            case 2:
                feedbackTitle = context.getString(R.string.feedback_title_2);
                break;
            case 3:
                feedbackTitle = context.getString(R.string.feedback_title_3);
                break;
            case 4:
                feedbackTitle = context.getString(R.string.feedback_title_4);
                break;
            case 5:
                feedbackTitle = context.getString(R.string.feedback_title_5);
                break;
            case 6:
                feedbackTitle = context.getString(R.string.feedback_title_6);
                break;
            default:
                feedbackTitle = context.getString(R.string.feedback_title_1);
                break;
        }

        return feedbackTitle;
    }
}
