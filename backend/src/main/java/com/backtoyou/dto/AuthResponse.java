package com.backtoyou.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private boolean success;
    private Boolean loggedIn;
    private String message;
    private UserDto user;

    public AuthResponse() {
    }

    public static AuthResponse success(String message, UserDto user) {
        AuthResponse resp = new AuthResponse();
        resp.setSuccess(true);
        resp.setMessage(message);
        resp.setUser(user);
        return resp;
    }

    public static AuthResponse error(String message) {
        AuthResponse resp = new AuthResponse();
        resp.setSuccess(false);
        resp.setMessage(message);
        return resp;
    }

    public static AuthResponse sessionCheck(boolean loggedIn, UserDto user) {
        AuthResponse resp = new AuthResponse();
        resp.setSuccess(true);
        resp.setLoggedIn(loggedIn);
        resp.setUser(user);
        return resp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Boolean getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}
