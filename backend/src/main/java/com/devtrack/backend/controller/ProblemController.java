package com.devtrack.backend.controller;


import com.devtrack.backend.dto.CreateProblemRequest;
import com.devtrack.backend.dto.PageResponse;
import com.devtrack.backend.dto.ProblemResponse;
import com.devtrack.backend.dto.UpdateProblemRequest;
import com.devtrack.backend.model.Difficulty;
import com.devtrack.backend.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.net.URI;

@RestController
@RequestMapping("/study-blocks/{studyBlockId}/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping
    public ResponseEntity<ProblemResponse> createProblem(
            @PathVariable Long studyBlockId,
            @Valid @RequestBody CreateProblemRequest request) {
        ProblemResponse createdProblem = problemService.createProblem(studyBlockId, request);
        URI location = URI.create("/study-blocks/" + studyBlockId + "/problems/" + createdProblem.getId());

        return ResponseEntity.created(location).body(createdProblem);
    }

    @GetMapping
    public PageResponse<ProblemResponse> getProblems(
            @PathVariable Long studyBlockId,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) Boolean solved,
            Pageable pageable) {

        return problemService.getProblems(studyBlockId, difficulty, solved, pageable);
    }

    @GetMapping("/{problemId}")
    public ProblemResponse getProblemById(
            @PathVariable Long studyBlockId,
            @PathVariable Long problemId) {

        return problemService.getProblemById(problemId, studyBlockId);
    }

    @PutMapping("/{problemId}")
    public ProblemResponse updateProblem(
            @PathVariable Long studyBlockId,
            @PathVariable Long problemId,
            @Valid @RequestBody UpdateProblemRequest request) {
        return problemService.updateProblem(problemId, studyBlockId, request);
    }

    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> deleteProblem(
            @PathVariable Long studyBlockId,
            @PathVariable Long problemId) {
        problemService.deleteProblem(problemId, studyBlockId);

        return ResponseEntity.noContent().build();
    }
}
