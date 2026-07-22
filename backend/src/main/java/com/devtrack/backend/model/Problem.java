package com.devtrack.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String difficulty;
    private String algorithm;
    private boolean solved;
    private String notes;
    private String url;

    public Problem(){}

    public Problem(String title) {

        validateTitle(title);

        this.title = title;
        this.solved = false;
    }

    public Problem(
            Long id,
            String title,
            String difficulty,
            String algorithm,
            boolean solved,
            String notes,
            String url) {

        validateTitle(title);

        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.algorithm = algorithm;
        this.solved = solved;
        this.notes = notes;
        this.url = url;
    }

    public Problem(
            String title,
            String difficulty,
            String algorithm,
            boolean solved,
            String notes,
            String url) {

        validateTitle(title);

        this.title = title;
        this.difficulty = difficulty;
        this.algorithm = algorithm;
        this.solved = solved;
        this.notes = notes;
        this.url = url;
    }

    public Long getId() {
        return this.id;
    }
    public String getTitle() {
        return this.title;
    }
    public String getDifficulty() {
        return this.difficulty;
    }
    public String getAlgorithm() {
        return this.algorithm;
    }
    public boolean isSolved() {
        return this.solved;
    }
    public String getNotes() {
        return this.notes;
    }
    public String getUrl() {
        return this.url;
    }

    public void setTitle(String title) {

        validateTitle(title);

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

    private void validateTitle(String title) {
        if (title == null ||title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
    }
}
