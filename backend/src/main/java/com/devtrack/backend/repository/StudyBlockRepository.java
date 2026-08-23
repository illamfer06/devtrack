package com.devtrack.backend.repository;

import com.devtrack.backend.model.StudyBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyBlockRepository extends JpaRepository<StudyBlock, Long> {

    Page<StudyBlock> findAll(Pageable pageable);
    Page<StudyBlock> findByActive(boolean activated, Pageable pageable);
}
