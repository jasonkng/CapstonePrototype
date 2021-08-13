package com.jason.capstone.Utility;

public class HomeModel {
    private String component;
    private String date;
    private String defect;
    private String length;
    private String priority;

    private HomeModel() {

    }

    public HomeModel(String component, String date, String defect, String length, String priority) {
        this.component = component;
        this.date = date;
        this.defect = defect;
        this.length = length;
        this.priority = priority;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDefect() {
        return defect;
    }

    public void setDefect(String defect) {
        this.defect = defect;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }


}
