package com.example.weatherapp.entities;

import java.util.ArrayList;

public class Daily {
    private String day;
    private String picPath;
    private String status;
    private int highTemp;
    private int lowTemp;

    public Daily(String day, String picPath, String status, int highTemp, int lowTemp) {
        this.day = day;
        this.picPath = picPath;
        this.status = status;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
    }

    public Daily(ArrayList<Daily> items) {
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(int highTemp) {
        this.highTemp = highTemp;
    }

    public int getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(int loeTemp) {
        this.lowTemp = loeTemp;
    }
}
