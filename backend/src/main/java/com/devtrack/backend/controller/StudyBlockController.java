package com.devtrack.backend.controller;

import com.devtrack.backend.dto.*;
import com.devtrack.backend.service.StudyBlockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/study-blocks")
public class StudyBlockController {

    private final StudyBlockService studyBlockService;

    public StudyBlockController(
            StudyBlockService studyBlockService) {
        this.studyBlockService = studyBlockService;
    }

    @PostMapping
    public ResponseEntity<StudyBlockResponse> createStudyBlock(@Valid @RequestBody CreateStudyBlockRequest request) {
        StudyBlockResponse createdStudyBlock = studyBlockService.createStudyBlock(request);
        URI location = URI.create("/study-blocks/" + createdStudyBlock.getId());

        return ResponseEntity.created(location).body(createdStudyBlock);
    }

    @GetMapping
    public PageResponse<StudyBlockResponse> getStudyBlocks(
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {

        return studyBlockService.getStudyBlocks(active, pageable);
    }

    @GetMapping("/{id}")
    public StudyBlockResponse getStudyBlockById(@PathVariable Long id) {
        return studyBlockService.getStudyBlockById(id);
    }

    @PutMapping("/{id}")
    public StudyBlockResponse updateStudyBlock(@PathVariable Long id, @Valid @RequestBody UpdateStudyBlockRequest request) {
        return studyBlockService.updateStudyBlock(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudyBlock(@PathVariable Long id) {
        studyBlockService.deleteStudyBlock(id);

        return ResponseEntity.noContent().build();
    }


}
