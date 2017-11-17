package com.example.quickstart;

/**
 * Created by Zsuzska on 2017. 10. 12..
 */

public class CalendarTask {

    private String name;
    private String description;
    private String date;
    private String state;

    public CalendarTask(String name, String description, String date, String state) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
