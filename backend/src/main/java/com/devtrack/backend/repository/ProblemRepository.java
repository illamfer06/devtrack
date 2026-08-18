package com.devtrack.backend.repository;

import com.devtrack.backend.model.Difficulty;
import com.devtrack.backend.model.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Page<Problem> findByDifficulty(Difficulty difficulty, Pageable pageable);
    Page<Problem> findBySolved(boolean solved, Pageable pageable);
    Page<Problem> findByDifficultyAndSolved(Difficulty difficulty, boolean solved, Pageable pageable);
}
