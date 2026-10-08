package com.backtoyou.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminUsersResponse {

    private boolean success;
    private String message;
    private Integer count;
    private List<AdminUserDto> users;

    public AdminUsersResponse() {
    }

    public static AdminUsersResponse success(List<AdminUserDto> users) {
        AdminUsersResponse r = new AdminUsersResponse();
        r.success = true;
        r.users = users;
        r.count = users != null ? users.size() : 0;
        return r;
    }

    public static AdminUsersResponse error(String message) {
        AdminUsersResponse r = new AdminUsersResponse();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<AdminUserDto> getUsers() {
        return users;
    }

    public void setUsers(List<AdminUserDto> users) {
        this.users = users;
    }
}
