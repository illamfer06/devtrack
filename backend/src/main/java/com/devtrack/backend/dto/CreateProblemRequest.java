package com.devtrack.backend.dto;

import com.devtrack.backend.model.Difficulty;
import jakarta.validation.constraints.NotBlank;

public class CreateProblemRequest {

    @NotBlank(message = "Title cannot be empty")
    private String title;
    private Difficulty difficulty;
    private String algorithm;
    private boolean solved;
    private String notes;
    private String url;

    public CreateProblemRequest() {
    }

    public String getTitle() {
        return title;
    }
    public Difficulty getDifficulty() {
        return difficulty;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public boolean isSolved() {
        return solved;
    }
    public String getNotes() {
        return notes;
    }
    public String getUrl() {
        return url;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    public void setSolved(boolean solved) {
        this.solved = solved;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}
