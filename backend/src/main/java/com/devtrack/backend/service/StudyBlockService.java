package com.devtrack.backend.service;

import com.devtrack.backend.dto.CreateStudyBlockRequest;
import com.devtrack.backend.dto.PageResponse;
import com.devtrack.backend.dto.StudyBlockResponse;
import com.devtrack.backend.dto.UpdateStudyBlockRequest;
import com.devtrack.backend.exception.StudyBlockNotEmptyException;
import com.devtrack.backend.exception.StudyBlockNotFoundException;
import com.devtrack.backend.model.StudyBlock;
import com.devtrack.backend.repository.ProblemRepository;
import com.devtrack.backend.repository.StudyBlockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudyBlockService {

    private final StudyBlockRepository studyBlockRepository;
    private final ProblemRepository problemRepository;

    public StudyBlockService(
            StudyBlockRepository studyBlockRepository,
            ProblemRepository problemRepository) {
        this.studyBlockRepository = studyBlockRepository;
        this.problemRepository = problemRepository;
    }

    public StudyBlockResponse createStudyBlock(CreateStudyBlockRequest request) {
        StudyBlock studyBlock = new StudyBlock(
                request.getTitle(),
                request.isActive()
        );

        StudyBlock savedStudyBlock = studyBlockRepository.save(studyBlock);

        return toStudyBlockResponse(savedStudyBlock);
    }

    public StudyBlockResponse getStudyBlockById(Long id) {
        StudyBlock studyBlock = findStudyBlockById(id);

        return toStudyBlockResponse(studyBlock);
    }

    public PageResponse<StudyBlockResponse> getStudyBlocks(Boolean active, Pageable pageable) {
        Page<StudyBlock> studyBlocks;

        if (active == null) {
            studyBlocks = studyBlockRepository.findAll(pageable);
        } else {
            studyBlocks = studyBlockRepository.findByActive(active, pageable);
        }

        List<StudyBlockResponse> studyBlockResponses = new ArrayList<>();

        for (StudyBlock studyBlock : studyBlocks.getContent()) {
            studyBlockResponses.add(toStudyBlockResponse(studyBlock));
        }

        return new PageResponse<>(
                studyBlockResponses,
                studyBlocks.getNumber(),
                studyBlocks.getSize(),
                studyBlocks.getTotalElements(),
                studyBlocks.getTotalPages()
        );
    }

    public StudyBlockResponse updateStudyBlock(Long id, UpdateStudyBlockRequest request) {
        StudyBlock studyBlock = findStudyBlockById(id);

        studyBlock.setTitle(request.getTitle());
        studyBlock.setActive(request.isActive());

        StudyBlock updatedStudyBlock = studyBlockRepository.save(studyBlock);

        return toStudyBlockResponse(updatedStudyBlock);
    }

    public void deleteStudyBlock(Long id) {
        StudyBlock studyBlock = findStudyBlockById(id);

        if (problemRepository.existsByStudyBlockId(id)) {
            throw new StudyBlockNotEmptyException("Study block must be empty to delete");
        }
        studyBlockRepository.delete(studyBlock);
    }

    private StudyBlock findStudyBlockById(Long id) {
        return studyBlockRepository.findById(id).orElseThrow(() -> new StudyBlockNotFoundException("Study block with id " + id + " was not found"));
    }

    private StudyBlockResponse toStudyBlockResponse(StudyBlock studyBlock) {
        return new StudyBlockResponse(
                studyBlock.getId(),
                studyBlock.getTitle(),
                studyBlock.isActive(),
                studyBlock.getCreatedAt(),
                studyBlock.getUpdatedAt()
        );
    }
}
