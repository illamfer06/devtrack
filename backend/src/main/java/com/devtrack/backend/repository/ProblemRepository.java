package com.devtrack.backend.repository;

import com.devtrack.backend.model.Difficulty;
import com.devtrack.backend.model.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findByIdAndStudyBlockId(Long problemId, Long studyBlockId);
    Page<Problem> findByStudyBlockId(Long studyBlockId, Pageable pageable);
    Page<Problem> findByStudyBlockIdAndDifficulty(Long studyBlockId, Difficulty difficulty, Pageable pageable);
    Page<Problem> findByStudyBlockIdAndSolved(Long studyBlockId, boolean solved, Pageable pageable);
    Page<Problem> findByStudyBlockIdAndDifficultyAndSolved(Long studyBlockId, Difficulty difficulty, boolean solved, Pageable pageable);
    boolean existsByStudyBlockId(Long studyBlockId);
}
