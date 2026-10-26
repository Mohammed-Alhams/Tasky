package com.mr_alhams.tasky;

import java.io.Serializable;

public class TaskTicket implements Serializable {
    private int id;
    private String title;
    private String location;
    private String status; // "Pending", "In Progress", "Completed"

    public TaskTicket(int id, String title, String location, String status) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}