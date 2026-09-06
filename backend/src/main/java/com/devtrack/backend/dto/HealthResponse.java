package com.devtrack.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the current application health status")
public class HealthResponse {

    @Schema(
            description = "Current health status of the application",
            example = "UP"
    )
    private String status;
    @Schema(
            description = "Application name",
            example = "DevTrack"
    )
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
