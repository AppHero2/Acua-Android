package com.mosili.acua.models;

import java.util.Map;

import static com.mosili.acua.utils.Util.getIntFromData;
import static com.mosili.acua.utils.Util.getStringFromData;

/**
 * Created by BKing on 12/6/2017.
 */

public class PayCard {
    private String token, last4;
    private String number, cvc;
    private String month, year;
    private String bankName;

    public PayCard(Map<String, Object> data){
        updateData(data);
    }

    public void updateData(Map<String, Object> data){
        this.token = getStringFromData("token", data);
        this.last4 = getStringFromData("last4", data);
        this.number = getStringFromData("number", data);
        this.month = getStringFromData("month", data);
        this.year = getStringFromData("year", data);
        this.cvc = getStringFromData("cvc", data);
        this.bankName = getStringFromData("bankName", data);
    }

    public String getBankName() {
        return bankName;
    }

    public String getCvc() {
        return cvc;
    }

    public String getMonth() {
        return month;
    }

    public String getYear() {
        return year;
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
