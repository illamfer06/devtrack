package com.devtrack.backend.controller;


import com.devtrack.backend.dto.CreateProblemRequest;
import com.devtrack.backend.dto.ProblemResponse;
import com.devtrack.backend.dto.UpdateProblemRequest;
import com.devtrack.backend.model.Difficulty;
import com.devtrack.backend.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public List<ProblemResponse> getProblems(
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) Boolean solved) {

        return problemService.getProblems(difficulty, solved);
    }

    @GetMapping("/{id}")
    public ProblemResponse getProblemById(@PathVariable Long id) {
        return problemService.getProblemById(id);
    }

    @PostMapping
    public ResponseEntity<ProblemResponse> createProblem(@Valid @RequestBody CreateProblemRequest request) {
        ProblemResponse createdProblem = problemService.createProblem(request);
        URI location = URI.create("/problems/" + createdProblem.getId());

        return ResponseEntity.created(location).body(createdProblem);
    }

    @PutMapping("/{id}")
    public ProblemResponse updateProblem(@PathVariable Long id, @Valid @RequestBody UpdateProblemRequest request) {
        return problemService.updateProblem(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);

        return ResponseEntity.noContent().build();
    }
}
