package com.tmsoftstudio.businesscontrol.backend;

import java.util.List;

public class LocationBean {

    private List<String> tasks;
    private String account;
    private double lat;
    private double lng;
    private String date;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
