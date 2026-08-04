package com.devtrack.backend.service;

import com.devtrack.backend.dto.CreateProblemRequest;
import com.devtrack.backend.dto.ProblemResponse;
import com.devtrack.backend.exception.ProblemNotFoundException;
import com.devtrack.backend.model.Problem;
import com.devtrack.backend.repository.ProblemRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
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

    public List<ProblemResponse> getAllProblems() {
        List<Problem> problems = problemRepository.findAll(Sort.by("id"));
        List<ProblemResponse> problemResponses = new ArrayList<>();

        for (Problem problem : problems) {
            problemResponses.add(toProblemResponse(problem));
        }

        return problemResponses;
    }

    public ProblemResponse getProblemById(Long id) {
        Problem problem = findProblemById(id);

        return toProblemResponse(problem);
    }

    public ProblemResponse updateProblem(Long id, CreateProblemRequest request) {
        Problem problem = findProblemById(id);

        problem.setTitle(request.getTitle());
        problem.setDifficulty(request.getDifficulty());
        problem.setAlgorithm(request.getAlgorithm());
        problem.setSolved(request.isSolved());
        problem.setNotes(request.getNotes());
        problem.setUrl(request.getUrl());

        Problem updatedProblem = problemRepository.save(problem);

        return toProblemResponse(updatedProblem);
    }

    public void deleteProblem(Long id) {
        Problem problem = findProblemById(id);

        problemRepository.delete(problem);
    }

    private Problem findProblemById(Long id) {
        return problemRepository.findById(id).orElseThrow(() -> new ProblemNotFoundException("Problem with id " + id + " was not found"));
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
