package com.devtrack.backend.service;

import com.devtrack.backend.dto.CreateProblemRequest;
import com.devtrack.backend.dto.ProblemResponse;
import com.devtrack.backend.model.Problem;
import com.devtrack.backend.repository.ProblemRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public List<ProblemResponse> getAllProblems() {
        List<Problem> problems = problemRepository.findAll();
        List<ProblemResponse> problemResponses = new ArrayList<>();

        for (Problem problem : problems) {
            problemResponses.add(toProblemResponse(problem));
        }

        return problemResponses;
    }

    public ProblemResponse createProblem(CreateProblemRequest request) {

        Problem problem = new Problem(
                request.getTitle(),
                request.getDifficulty(),
                request.getAlgorithm(),
                request.isSolved(),
                request.getNotes(),
                request.getUrl());

        Problem savedProblem = problemRepository.save(problem);

        return toProblemResponse(savedProblem);
    }

    private ProblemResponse toProblemResponse(Problem problem) {
        return new ProblemResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getDifficulty(),
                problem.getAlgorithm(),
                problem.isSolved(),
                problem.getNotes(),
                problem.getUrl()
        );
    }
}
