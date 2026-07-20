package com.devtrack.backend.dto;

public class CreateProblemRequest {

    private String title;
    private String difficulty;
    private String algorithm;
    private boolean solved;
    private String notes;
    private String url;

    public CreateProblemRequest() {
    }

    public String getTitle() {
        return title;
    }
    public String getDifficulty() {
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
    public void setDifficulty(String difficulty) {
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
