package com.devtrack.backend.controller;


import com.devtrack.backend.dto.*;
import com.devtrack.backend.model.Difficulty;
import com.devtrack.backend.service.ProblemService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;

import java.net.URI;

@Tag(
        name = "Problems",
        description = "Operations for managing coding problems inside study blocks"
)
@RestController
@RequestMapping("/study-blocks/{studyBlockId}/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @Operation(
            summary = "Create a problem",
            description = "Creates a new coding problem inside the specified study block."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Problem created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Study block not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ProblemResponse> createProblem(
            @Parameter(
                    description = "ID of the study block where the problem will be created",
                    example = "1"
            )
            @PathVariable Long studyBlockId,
            @Valid @RequestBody CreateProblemRequest request) {
        ProblemResponse createdProblem = problemService.createProblem(studyBlockId, request);
        URI location = URI.create("/study-blocks/" + studyBlockId + "/problems/" + createdProblem.getId());

        return ResponseEntity.created(location).body(createdProblem);
    }

    @Operation(
            summary = "Get problems from a study block",
            description = "Returns a paginated list of problems belonging to the specified study block. " +
                    "Results can optionally be filtered by difficulty and solved status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Problems retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter or pagination parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Study block not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping
    public PageResponse<ProblemResponse> getProblems(
            @Parameter(
                    description = "ID of the study block",
                    example = "1"
            )
            @PathVariable Long studyBlockId,

            @Parameter(
                    description = "Optional difficulty filter",
                    example = "EASY"
            )
            @RequestParam(required = false) Difficulty difficulty,

            @Parameter(
                    description = "Optional solved status filter",
                    example = "true"
            )
            @RequestParam(required = false) Boolean solved,

            @ParameterObject Pageable pageable) {

        return problemService.getProblems(studyBlockId, difficulty, solved, pageable);
    }

    @Operation(
            summary = "Get a problem by ID",
            description = "Returns a specific problem if it belongs to the specified study block."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Problem retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Study block or problem not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{problemId}")
    public ProblemResponse getProblemById(
            @Parameter(
                    description = "ID of the study block",
                    example = "1"
            )
            @PathVariable Long studyBlockId,

            @Parameter(
                    description = "ID of the problem",
                    example = "10"
            )
            @PathVariable Long problemId) {

        return problemService.getProblemById(problemId, studyBlockId);
    }

    @Operation(
            summary = "Update a problem",
            description = "Updates an existing problem that belongs to the specified study block."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Problem updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Study block or problem not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PutMapping("/{problemId}")
    public ProblemResponse updateProblem(
            @Parameter(
                    description = "ID of the study block",
                    example = "1"
            )
            @PathVariable Long studyBlockId,

            @Parameter(
                    description = "ID of the problem",
                    example = "10"
            )
            @PathVariable Long problemId,

            @Valid @RequestBody UpdateProblemRequest request) {
        return problemService.updateProblem(problemId, studyBlockId, request);
    }

    @Operation(
            summary = "Delete a problem",
            description = "Deletes a problem from the specified study block."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Problem deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Study block or problem not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> deleteProblem(
            @Parameter(
                    description = "ID of the study block",
                    example = "1"
            )
            @PathVariable Long studyBlockId,

            @Parameter(
                    description = "ID of the problem",
                    example = "10"
            )
            @PathVariable Long problemId) {
        problemService.deleteProblem(problemId, studyBlockId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete multiple problems",
            description = "Deletes multiple problems from a study block. All selected problems must belong to the specified study block."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Problems deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Study block not found or one or more selected problems do not belong to it",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid problem selection",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDeleteProblems(
            @Parameter(
                    description = "ID of the study block",
                    example = "1"
            )
            @PathVariable Long studyBlockId,
            @Valid @RequestBody BulkDeleteProblemsRequest request) {

        problemService.bulkDeleteProblems(studyBlockId, request);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Clear all problems",
            description = "Deletes all problems from a study block without deleting the study block itself."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "All problems deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Study block not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearProblems(
            @Parameter(
                    description = "ID of the study block",
                    example = "1"
            )
            @PathVariable Long studyBlockId) {

        problemService.clearProblems(studyBlockId);

        return ResponseEntity.noContent().build();
    }
}
