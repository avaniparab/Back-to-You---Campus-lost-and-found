package com.backtoyou.dto;

public class ItemUpdateRequest {

    private String title;
    private String description;
    private String category;
    private String location;
    private String date;
    private String status;

    public ItemUpdateRequest() {
    }

    public ItemUpdateRequest(String title, String description, String category, String location, String date, String status) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.date = date;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
