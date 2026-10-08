package com.backtoyou.dto;

public class AdminCreateUserRequest {

    private String name;
    private String fullname;
    private String email;
    private String password;
    private String role;
    private String status;

    public AdminCreateUserRequest() {
    }

    public AdminCreateUserRequest(String name, String email, String password, String role, String status) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public String getName() {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        if (fullname != null && !fullname.trim().isEmpty()) {
            return fullname.trim();
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
