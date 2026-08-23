package com.devtrack.backend.dto;

import java.time.LocalDateTime;

public class StudyBlockResponse {

    private Long id;
    private String title;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StudyBlockResponse(
            Long id,
            String title,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id;}
    public String getTitle() { return title;}
    public boolean isActive() { return active;}
    public LocalDateTime getCreatedAt() { return createdAt;}
    public LocalDateTime getUpdatedAt() { return updatedAt;}

}
