package com.devtrack.backend.repository;

import com.devtrack.backend.model.StudyBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudyBlockRepository extends JpaRepository<StudyBlock, Long> {

    Optional<StudyBlock> findByIdAndUserId(Long studyBlockId, Long userId);
    Page<StudyBlock> findByUserId(Long userId, Pageable pageable);
    Page<StudyBlock> findByUserIdAndActive(Long userId, boolean activated, Pageable pageable);
    @Modifying
    @Query("""
            DELETE FROM StudyBlock sb WHERE sb.user.id = :userId
           """)
    int deleteAllByUserId(@Param("userId") Long userId);
}
