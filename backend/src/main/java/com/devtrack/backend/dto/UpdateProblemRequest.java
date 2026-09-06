package com.devtrack.backend.dto;

import com.devtrack.backend.model.Difficulty;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for updating an existing coding problem")
public class UpdateProblemRequest {

    @Schema(
            description = "Title of the coding problem",
            example = "Two Sum",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Title cannot be empty")
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

    public UpdateProblemRequest(){
    }

    public String getTitle() { return title;}
    public Difficulty getDifficulty() { return difficulty;}
    public String getAlgorithm() { return algorithm;}
    public boolean isSolved() { return solved;}
    public String getNotes() { return notes;}
    public String getUrl() { return url;}

    public void setTitle(String title) { this.title = title;}
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty;}
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm;}
    public void setSolved(boolean solved) { this.solved = solved;}
    public void setNotes(String notes) {this.notes = notes;}
    public void setUrl(String url) { this.url = url;}
}
