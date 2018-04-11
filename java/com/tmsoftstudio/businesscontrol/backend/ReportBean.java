package com.tmsoftstudio.businesscontrol.backend;

import java.util.List;

public class ReportBean {
    private List<String> tasks;
    private String account;
    private String report;
    private List<String> images;
    private Boolean state;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getImages() {
        return images;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getReport() {
        return report;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }
}
