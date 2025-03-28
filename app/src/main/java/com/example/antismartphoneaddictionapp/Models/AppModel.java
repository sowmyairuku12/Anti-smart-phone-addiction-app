package com.example.antismartphoneaddictionapp.Models;

import android.graphics.drawable.Drawable;

public class AppModel {

    public int id;
    public Drawable appIcon; // You may add get this usage data also, if you wish.
    public String appName, packageName;
    public long timeInForeground;
    public int launchCount;

    public AppModel() {
    }

    public AppModel(String pName) {
        this.packageName = pName;
    }

    public AppModel(String appName, String packageName, long timeInForeground) {
        this.appName = appName;
        this.packageName = packageName;
        this.timeInForeground = timeInForeground;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getTimeInForeground() {
        return timeInForeground;
    }

    public void setTimeInForeground(long timeInForeground) {
        this.timeInForeground = timeInForeground;
    }

    public int getLaunchCount() {
        return launchCount;
    }

    public void setLaunchCount(int launchCount) {
        this.launchCount = launchCount;
    }
}