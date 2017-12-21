package com.mosili.acua.models;

import java.util.Map;

import static com.mosili.acua.utils.Util.getStringFromData;

/**
 * Created by BKing on 12/6/2017.
 */

public class PayCard {
    private String token, last4;
    private String number, endAt, cvv;
    private String bankName;

    public PayCard(Map<String, Object> data){
        updateData(data);
    }

    public void updateData(Map<String, Object> data){
        this.token = getStringFromData("token", data);
        this.last4 = getStringFromData("last4", data);
        this.number = getStringFromData("number", data);
        this.endAt = getStringFromData("endAt", data);
        this.cvv = getStringFromData("cvc", data);
        this.bankName = getStringFromData("bankName", data);
    }

    public String getBankName() {
        return bankName;
    }

    public String getCvv() {
        return cvv;
    }

    public String getEndAt() {
        return endAt;
    }

    public String getLast4() {
        return last4;
    }

    public String getNumber() {
        return number;
    }

    public String getToken() {
        return token;
    }
}
