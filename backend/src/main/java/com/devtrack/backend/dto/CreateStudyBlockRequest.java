package com.devtrack.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for creating a study block")
public class CreateStudyBlockRequest {

    @Schema(
            description = "Title of the study block",
            example = "Java",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Title cannot be empty")
    private String title;
    @Schema(
            description = "Indicates whether the study block is active",
            example = "true"
    )
    private boolean active;

    public CreateStudyBlockRequest() {}

    public String getTitle() { return title;}
    public boolean isActive() { return active;}

    public void setTitle(String title) { this.title = title;}
    public void setActive(boolean active) { this.active = active;}
}

