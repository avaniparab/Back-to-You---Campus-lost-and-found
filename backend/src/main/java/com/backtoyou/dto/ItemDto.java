package com.backtoyou.dto;

import com.backtoyou.entity.Item;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemDto {

    private Integer id;
    private Integer user_id;
    private String title;
    private String description;
    private String category;
    private String location;
    private String date;
    private String type;
    private String image;
    private String status;
    private String created_at;
    private String reporter_name;
    private String reporter_email;
    private String reporter_role;

    public ItemDto() {
    }

    public static ItemDto fromEntity(Item item, boolean includeReporterRole) {
        if (item == null) {
            return null;
        }

        ItemDto dto = new ItemDto();
        dto.id = item.getId();
        if (item.getUser() != null) {
            dto.user_id = item.getUser().getId();
            dto.reporter_name = item.getUser().getName();
            dto.reporter_email = item.getUser().getEmail();
            if (includeReporterRole && item.getUser().getRole() != null) {
                dto.reporter_role = item.getUser().getRole().name();
            }
        }
        dto.title = item.getTitle();
        dto.description = item.getDescription();
        dto.category = item.getCategory();
        dto.location = item.getLocation();
        dto.date = item.getDate() != null ? item.getDate().toString() : null;
        dto.type = item.getType() != null ? item.getType().name() : null;
        dto.image = item.getImage();
        dto.status = item.getStatus() != null ? item.getStatus().name() : null;
        dto.created_at = item.getCreatedAt() != null ? 
            item.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;

        return dto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getReporter_name() {
        return reporter_name;
    }

    public void setReporter_name(String reporter_name) {
        this.reporter_name = reporter_name;
    }

    public String getReporter_email() {
        return reporter_email;
    }

    public void setReporter_email(String reporter_email) {
        this.reporter_email = reporter_email;
    }

    public String getReporter_role() {
        return reporter_role;
    }

    public void setReporter_role(String reporter_role) {
        this.reporter_role = reporter_role;
    }
}
