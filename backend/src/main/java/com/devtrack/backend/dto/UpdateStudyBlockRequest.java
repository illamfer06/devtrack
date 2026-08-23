package com.devtrack.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateStudyBlockRequest {

    @NotBlank(message = "Title cannot be empty")
    private String title;
    private boolean active;

    public UpdateStudyBlockRequest() {}

    public String getTitle() { return title;}
    public boolean isActive() { return active;}

    public void setTitle(String title) { this.title = title;}
    public void setActive(boolean active) { this.active = active;}
}
