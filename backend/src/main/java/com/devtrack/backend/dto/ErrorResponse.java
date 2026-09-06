package com.devtrack.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned when an error occurs")
public class ErrorResponse {

    @Schema(
            description = "HTTP status code",
            example = "404"
    )
    private int status;
    @Schema(
            description = "HTTP error name",
            example = "Not Found"
    )
    private String error;
    @Schema(
            description = "Detailed description of the error",
            example = "Study block with id 99 was not found"
    )
    private String message;
    @Schema(
            description = "Path of the request that caused the error",
            example = "/study-blocks/99"
    )
    private String path;

    public ErrorResponse(
            int status,
            String error,
            String message,
            String path) {

        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public int getStatus() {
        return status;
    }
    public String getError() {
        return error;
    }
    public String getMessage() {
        return message;
    }
    public String getPath() { return path;}
}
