package com.backtoyou.dto;

public class AdminUpdateUserRequest {

    private String name;
    private String email;
    private String role;
    private String status;
    private String password;

    public AdminUpdateUserRequest() {
    }

    public AdminUpdateUserRequest(String name, String email, String role, String status, String password) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
