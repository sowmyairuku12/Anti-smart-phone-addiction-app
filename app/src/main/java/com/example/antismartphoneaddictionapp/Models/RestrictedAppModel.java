package com.example.antismartphoneaddictionapp.Models;

import java.io.Serializable;

public class RestrictedAppModel implements Serializable {

    private int id;
    private String packageName;
    private String date;
    private String time;
    private String expiryTime;

    public RestrictedAppModel() {
    }

    // Constructor
    public RestrictedAppModel(String packageName, String date, String time, String expiryTime) {
        this.packageName = packageName;
        this.date = date;
        this.time = time;
        this.expiryTime = expiryTime;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
    }

}
