package com.devtrack.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public class BulkDeleteProblemsRequest {

    @Schema(
            description = "IDs of the problems to delete",
            example = "[1, 2, 3]"
    )
    @NotEmpty(message = "At least one problem must be selected")
    private Set<@NotNull(message = "Problem id cannot be null") Long> problemIds;

    public BulkDeleteProblemsRequest() {}

    public Set<Long> getProblemIds() { return problemIds;}
    public void setProblemIds(Set<Long> problemIds) {
        this.problemIds = problemIds;
    }
}
