package com.devtrack.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response containing the data of a study block")
public class StudyBlockResponse {

    @Schema(
            description = "Unique identifier of the study block",
            example = "1"
    )
    private Long id;
    @Schema(
            description = "Title of the study block",
            example = "Java"
    )
    private String title;
    @Schema(
            description = "Indicates whether the study block is active",
            example = "true"
    )
    private boolean active;
    @Schema(
            description = "Date and time when the study block was created",
            example = "2026-08-22T18:00:00"
    )
    private LocalDateTime createdAt;
    @Schema(
            description = "Date and time when the study block was last updated",
            example = "2026-08-22T18:30:00"
    )
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
