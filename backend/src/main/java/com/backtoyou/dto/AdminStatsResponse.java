package com.backtoyou.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminStatsResponse {

    private boolean success;
    private String message;
    private AdminStatsDto stats;

    public AdminStatsResponse() {
    }

    public static AdminStatsResponse success(AdminStatsDto stats) {
        AdminStatsResponse r = new AdminStatsResponse();
        r.success = true;
        r.stats = stats;
        return r;
    }

    public static AdminStatsResponse error(String message) {
        AdminStatsResponse r = new AdminStatsResponse();
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

    public AdminStatsDto getStats() {
        return stats;
    }

    public void setStats(AdminStatsDto stats) {
        this.stats = stats;
    }
}
