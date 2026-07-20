package com.devtrack.backend.dto;

public class ProblemResponse {

    private Long id;
    private String title;
    private String difficulty;
    private String algorithm;
    private boolean solved;
    private String notes;
    private String url;

    public ProblemResponse(
            Long id,
            String title,
            String difficulty,
            String algorithm,
            boolean solved,
            String notes,
            String url) {

        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.algorithm = algorithm;
        this.solved = solved;
        this.notes = notes;
        this.url = url;
    }

    public Long getId() {
        return id;
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
}
