package com.devtrack.backend.controller;


import com.devtrack.backend.dto.CreateProblemRequest;
import com.devtrack.backend.dto.ProblemResponse;
import com.devtrack.backend.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping("/problems")
    public List<ProblemResponse> getProblems() {
        return problemService.getAllProblems();
    }

    @GetMapping("/problems/{id}")
    public ProblemResponse getProblemById(@PathVariable Long id) { return problemService.getProblemById(id); }

    @PostMapping("/problems")
    public ProblemResponse createProblem(@Valid @RequestBody CreateProblemRequest request) { return problemService.createProblem(request); }

    @PutMapping("/problems/{id}")
    public ProblemResponse updateProblem(@PathVariable Long id, @Valid @RequestBody CreateProblemRequest request) { return problemService.updateProblem(id, request); }

    @DeleteMapping("/problems/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);

        return ResponseEntity.noContent().build();
    }
}
