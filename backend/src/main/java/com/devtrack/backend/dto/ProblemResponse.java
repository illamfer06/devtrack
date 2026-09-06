package com.devtrack.backend.dto;

import com.devtrack.backend.model.Difficulty;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the data of a coding problem")
public class ProblemResponse {

    @Schema(
            description = "Unique identifier of the problem",
            example = "1"
    )
    private Long id;
    @Schema(
            description = "Title of the coding problem",
            example = "Two Sum"
    )
    private String title;
    @Schema(
            description = "Difficulty level of the problem",
            example = "EASY"
    )
    private Difficulty difficulty;
    @Schema(
            description = "Main algorithm or technique used to solve the problem",
            example = "Hash Map"
    )
    private String algorithm;
    @Schema(
            description = "Indicates whether the problem has been solved",
            example = "true"
    )
    private boolean solved;
    @Schema(
            description = "Optional notes about the problem or solution",
            example = "Review the O(n) solution"
    )
    private String notes;
    @Schema(
            description = "Optional URL of the original problem",
            example = "https://leetcode.com/problems/two-sum/"
    )
    private String url;
    @Schema(
            description = "Date and time when the problem was created",
            example = "2026-08-22T18:00:00"
    )
    private LocalDateTime createdAt;
    @Schema(
            description = "Date and time when the problem was last updated",
            example = "2026-08-22T18:30:00"
    )
    private LocalDateTime updatedAt;

    public ProblemResponse(
            Long id,
            String title,
            Difficulty difficulty,
            String algorithm,
            boolean solved,
            String notes,
            String url,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.algorithm = algorithm;
        this.solved = solved;
        this.notes = notes;
        this.url = url;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public Difficulty getDifficulty() {
        return difficulty;
    }
    public String getAlgorithm() {return algorithm;}
    public boolean isSolved() {
        return solved;
    }
    public String getNotes() {
        return notes;
    }
    public String getUrl() {
        return url;
    }
    public LocalDateTime getCreatedAt() {return createdAt;}
    public LocalDateTime getUpdatedAt() {return updatedAt;}
}
