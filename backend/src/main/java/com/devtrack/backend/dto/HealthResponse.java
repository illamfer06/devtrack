package com.devtrack.backend.dto;

public class HealthResponse {

    private String status;
    private String application;

    public HealthResponse(String status, String application) {
        this.status = status;
        this.application = application;
    }

    public String getStatus() {
        return this.status;
    }
    public String getApplication() {
        return this.application;
    }
}
