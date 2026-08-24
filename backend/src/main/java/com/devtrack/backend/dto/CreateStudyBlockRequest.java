package com.devtrack.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateStudyBlockRequest {

    @NotBlank(message = "Title cannot be empty")
    String title;
    boolean active;

    public CreateStudyBlockRequest() {}

    public String getTitle() { return title;}
    public boolean isActive() { return active;}

    public void setTitle(String title) { this.title = title;}
    public void setActive(boolean active) { this.active = active;}
}

