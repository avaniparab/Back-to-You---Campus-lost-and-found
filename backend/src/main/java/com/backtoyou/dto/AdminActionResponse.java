package com.backtoyou.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminActionResponse {

    private boolean success;
    private String message;
    private Integer user_id;

    public AdminActionResponse() {
    }

    public static AdminActionResponse success(String message) {
        AdminActionResponse r = new AdminActionResponse();
        r.success = true;
        r.message = message;
        return r;
    }

    public static AdminActionResponse createSuccess(String message, Integer userId) {
        AdminActionResponse r = new AdminActionResponse();
        r.success = true;
        r.message = message;
        r.user_id = userId;
        return r;
    }

    public static AdminActionResponse error(String message) {
        AdminActionResponse r = new AdminActionResponse();
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

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
}
