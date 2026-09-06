package com.devtrack.backend.controller;

import com.devtrack.backend.dto.*;
import com.devtrack.backend.service.StudyBlockService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;

import java.net.URI;

@Tag(
        name = "Study Blocks",
        description = "Operations for managing study blocks"
)
@RestController
@RequestMapping("/study-blocks")
public class StudyBlockController {

    private final StudyBlockService studyBlockService;

    public StudyBlockController(
            StudyBlockService studyBlockService) {
        this.studyBlockService = studyBlockService;
    }

    @Operation(
            summary = "Create a study block",
            description = "Creates a new study block."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Study block created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<StudyBlockResponse> createStudyBlock(@Valid @RequestBody CreateStudyBlockRequest request) {
        StudyBlockResponse createdStudyBlock = studyBlockService.createStudyBlock(request);
        URI location = URI.create("/study-blocks/" + createdStudyBlock.getId());

        return ResponseEntity.created(location).body(createdStudyBlock);
    }

    @Operation(
            summary = "Get study blocks",
            description = "Returns a paginated list of study blocks. " +
                    "Results can optionally be filtered by active status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Study blocks retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter or pagination parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping
    public PageResponse<StudyBlockResponse> getStudyBlocks(
            @Parameter(
                    description = "Optional filter by active status",
                    example = "true"
            )
            @RequestParam(required = false) Boolean active,

            @ParameterObject Pageable pageable) {

        return studyBlockService.getStudyBlocks(active, pageable);
    }

    @Operation(
            summary = "Get a study block by ID",
            description = "Returns a specific study block."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Study block retrieved successfully"
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
    @GetMapping("/{studyBlockId}")
    public StudyBlockResponse getStudyBlockById(
            @Parameter(
                    description = "ID of the study block",
                    example = "1"
            )
            @PathVariable Long studyBlockId) {
        return studyBlockService.getStudyBlockById(studyBlockId);
    }

    @Operation(
            summary = "Update a study block",
            description = "Updates an existing study block."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Study block updated successfully"
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
    @PutMapping("/{studyBlockId}")
    public StudyBlockResponse updateStudyBlock(
            @Parameter(
                    description = "ID of the study block",
                    example = "1"
            )
            @PathVariable Long studyBlockId,

            @Valid @RequestBody UpdateStudyBlockRequest request) {
        return studyBlockService.updateStudyBlock(studyBlockId, request);
    }

    @Operation(
            summary = "Delete a study block",
            description = "Deletes a study block."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Study block deleted successfully"
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
    @DeleteMapping("/{studyBlockId}")
    public ResponseEntity<Void> deleteStudyBlock(
            @Parameter(
                    description = "ID of the study block",
                    example = "1"
            )
            @PathVariable Long studyBlockId) {
        studyBlockService.deleteStudyBlock(studyBlockId);

        return ResponseEntity.noContent().build();
    }
}
