package com.mosili.acua.models;

import android.util.Log;

import com.mosili.acua.utils.Util;
import com.stripe.android.model.Token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.mosili.acua.utils.Util.getIntFromData;
import static com.mosili.acua.utils.Util.getMapDataFromData;
import static com.mosili.acua.utils.Util.getStringFromData;

/**
 * Created by BKing on 12/5/2017.
 */

public class User {

    private String idx, firstname, lastname, email, photo, phone, bio, pushToken;
    private int userType = 0; //0 = customer, 1 = service, 2 = admin
    private PayCard payCard;
    public User(Map<String, Object> data){
        updateData(data);
    }

    public void updateData(Map<String, Object> data){
        this.idx = getStringFromData("uid", data);
        this.firstname = getStringFromData("firstname", data);
        this.lastname = getStringFromData("lastname", data);
        this.email = getStringFromData("email", data);
        this.photo = getStringFromData("photo", data);
        this.phone = getStringFromData("phone", data);
        this.bio = getStringFromData("bio", data);
        this.pushToken = getStringFromData("pushToken", data);
        this.userType = getIntFromData("userType", data);
        Map<String, Object> cardData = getMapDataFromData("payCard", data);
        this.payCard = new PayCard(cardData);
    }

    public String getBio() {
        return bio;
    }

    public String getIdx() {
        return idx;
    }

    public int getUserType() {
        return userType;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhoto() {
        return photo;
    }

    public String getPushToken() {
        return pushToken;
    }

    public String getFullName () {
        return  this.firstname + " " + this.getLastname();
    }

    public PayCard getPayCard() {
        return payCard;
    }
}
