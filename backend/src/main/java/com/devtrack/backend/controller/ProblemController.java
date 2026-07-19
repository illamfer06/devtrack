package com.devtrack.backend.controller;


import com.devtrack.backend.model.Problem;
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
    public List<Problem> getProblems() {
        return problemService.getAllProblems();
    }

    @PostMapping("/problems")
    public Problem createProblem(@RequestBody Problem problem) {
        return problemService.createProblem(problem);
    }

}
