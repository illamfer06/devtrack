package com.devtrack.backend.controller;


import com.devtrack.backend.dto.CreateProblemRequest;
import com.devtrack.backend.dto.ProblemResponse;
import com.devtrack.backend.service.ProblemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/problems")
    public ProblemResponse createProblem(@RequestBody CreateProblemRequest request) {
        return problemService.createProblem(request);
    }

}
