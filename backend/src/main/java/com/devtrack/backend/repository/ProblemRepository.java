package com.devtrack.backend.repository;

import com.devtrack.backend.model.Difficulty;
import com.devtrack.backend.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByDifficulty(Difficulty difficulty);
}
