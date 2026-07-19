package com.devtrack.backend.service;

import com.devtrack.backend.model.Problem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProblemService {

    private final List<Problem> problems = new ArrayList<>();

    public ProblemService() {
        problems.add(new Problem("Two Sum"));
        problems.add(new Problem("Two Sum2"));
    }

    public List<Problem> getAllProblems() {
        return problems;
    }

    public Problem createProblem(Problem problem) {
        problems.add(problem);
        return problem;
    }

}
