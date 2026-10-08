package com.backtoyou.dto;

public class AdminUpdateStatusRequest {

    private Integer id;
    private String status;

    public AdminUpdateStatusRequest() {
    }

    public AdminUpdateStatusRequest(Integer id, String status) {
        this.id = id;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
