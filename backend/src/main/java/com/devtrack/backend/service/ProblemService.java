package com.devtrack.backend.service;

import com.devtrack.backend.dto.*;
import com.devtrack.backend.exception.ProblemNotFoundException;
import com.devtrack.backend.exception.StudyBlockNotFoundException;
import com.devtrack.backend.model.Difficulty;
import com.devtrack.backend.model.Problem;
import com.devtrack.backend.model.StudyBlock;
import com.devtrack.backend.repository.ProblemRepository;
import com.devtrack.backend.repository.StudyBlockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final StudyBlockRepository studyBlockRepository;

    public ProblemService(
            ProblemRepository problemRepository,
            StudyBlockRepository studyBlockRepository) {

        this.problemRepository = problemRepository;
        this.studyBlockRepository = studyBlockRepository;
    }

    public ProblemResponse createProblem(Long studyBlockId, CreateProblemRequest request) {
        StudyBlock studyBlock = studyBlockRepository.findById(studyBlockId)
                .orElseThrow(() -> new StudyBlockNotFoundException("Study block with id " + studyBlockId + " was not found"));

        Problem problem = new Problem(
                request.getTitle(),
                request.getDifficulty(),
                request.getAlgorithm(),
                request.isSolved(),
                request.getNotes(),
                request.getUrl());

        problem.setStudyBlock(studyBlock);

        Problem savedProblem = problemRepository.save(problem);

        return toProblemResponse(savedProblem);
    }

    public PageResponse<ProblemResponse> getProblems(Long studyBlockId, Difficulty difficulty, Boolean solved, Pageable pageable) {
        studyBlockRepository.findById(studyBlockId)
                .orElseThrow(() -> new StudyBlockNotFoundException("Study block with id " + studyBlockId + " was not found"));

        Page<Problem> problems;

        if (difficulty == null && solved == null) {
            problems = problemRepository.findByStudyBlockId(studyBlockId, pageable);
        } else if (solved == null) {
            problems = problemRepository.findByStudyBlockIdAndDifficulty(studyBlockId, difficulty, pageable);
        } else if (difficulty == null) {
            problems = problemRepository.findByStudyBlockIdAndSolved(studyBlockId, solved, pageable);
        } else {
            problems = problemRepository.findByStudyBlockIdAndDifficultyAndSolved(studyBlockId, difficulty, solved, pageable);
        }

        List<ProblemResponse> problemResponses = new ArrayList<>();

        for (Problem problem : problems.getContent()) {
            problemResponses.add(toProblemResponse(problem));
        }

        return new PageResponse<> (
                problemResponses,
                problems.getNumber(),
                problems.getSize(),
                problems.getTotalElements(),
                problems.getTotalPages()
        );
    }

    public ProblemResponse getProblemById(Long problemId, Long studyBlockId) {
        Problem problem = findProblemByIdAndStudyBlockId(problemId, studyBlockId);

        return toProblemResponse(problem);
    }

    public ProblemResponse updateProblem(Long problemId, Long studyBlockId, UpdateProblemRequest request) {
        Problem problem = findProblemByIdAndStudyBlockId(problemId, studyBlockId);

        problem.setTitle(request.getTitle());
        problem.setDifficulty(request.getDifficulty());
        problem.setAlgorithm(request.getAlgorithm());
        problem.setSolved(request.isSolved());
        problem.setNotes(request.getNotes());
        problem.setUrl(request.getUrl());

        Problem updatedProblem = problemRepository.save(problem);

        return toProblemResponse(updatedProblem);
    }

    public void deleteProblem(Long problemId, Long studyBlockId) {
        Problem problem = findProblemByIdAndStudyBlockId(problemId, studyBlockId);

        problemRepository.delete(problem);
    }

    @Transactional
    public void bulkDeleteProblems(
            Long studyBlockId,
            BulkDeleteProblemsRequest request) {

        findStudyBlockById(studyBlockId);

        Set<Long> problemIds = request.getProblemIds();

        List<Problem> problems = problemRepository.findByIdInAndStudyBlockId(problemIds, studyBlockId);

        if (problems.size() != problemIds.size()) {
            throw new ProblemNotFoundException(
                    "One or more selected problems were not found in study block " + studyBlockId
            );
        }
        problemRepository.deleteAllInBatch(problems);
    }

    private StudyBlock findStudyBlockById(Long studyBlockId) {
        return studyBlockRepository.findById(studyBlockId)
                .orElseThrow(() -> new StudyBlockNotFoundException("Study block with id " + studyBlockId + " was not found"));
    }

    private Problem findProblemByIdAndStudyBlockId(Long problemId, Long studyBlockId) {
       findStudyBlockById(studyBlockId);

        return problemRepository.findByIdAndStudyBlockId(problemId, studyBlockId)
                .orElseThrow(() -> new ProblemNotFoundException("Problem with id " + problemId + " was not found in study block " + studyBlockId));
    }

    private ProblemResponse toProblemResponse(Problem problem) {
        return new ProblemResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getDifficulty(),
                problem.getAlgorithm(),
                problem.isSolved(),
                problem.getNotes(),
                problem.getUrl(),
                problem.getCreatedAt(),
                problem.getUpdatedAt()
        );
    }
}
