package com.backtoyou.dto;

import com.backtoyou.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.format.DateTimeFormatter;

public class AdminUserDto {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer id;
    private String name;
    private String email;
    private String role;
    private String status;

    @JsonProperty("created_at")
    private String createdAt;

    public AdminUserDto() {
    }

    public AdminUserDto(Integer id, String name, String email, String role, String status, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static AdminUserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        String createdStr = user.getCreatedAt() != null ? user.getCreatedAt().format(FORMATTER) : null;
        return new AdminUserDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole() != null ? user.getRole().name() : null,
            user.getStatus() != null ? user.getStatus().name() : null,
            createdStr
        );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
