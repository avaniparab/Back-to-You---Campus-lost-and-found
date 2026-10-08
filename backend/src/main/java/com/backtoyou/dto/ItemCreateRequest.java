package com.backtoyou.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ItemCreateRequest {

    @JsonAlias({"item_name", "title"})
    private String title;

    private String category;
    private String description;
    private String location;

    @JsonAlias({"date_lost", "date_found", "date"})
    private String date;

    private String type;
    private String image;

    public ItemCreateRequest() {
    }

    public ItemCreateRequest(String title, String category, String description, String location, String date, String type, String image) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.location = location;
        this.date = date;
        this.type = type;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
