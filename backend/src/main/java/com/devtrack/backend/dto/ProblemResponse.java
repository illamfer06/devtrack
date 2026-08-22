package com.devtrack.backend.dto;

import com.devtrack.backend.model.Difficulty;

import java.time.LocalDateTime;

public class ProblemResponse {

    private Long id;
    private String title;
    private Difficulty difficulty;
    private String algorithm;
    private boolean solved;
    private String notes;
    private String url;
    private LocalDateTime createdAt;
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
