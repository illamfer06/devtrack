package com.devtrack.backend.service;

import com.devtrack.backend.dto.*;
import com.devtrack.backend.exception.StudyBlockNotEmptyException;
import com.devtrack.backend.exception.StudyBlockNotFoundException;
import com.devtrack.backend.model.StudyBlock;
import com.devtrack.backend.repository.ProblemRepository;
import com.devtrack.backend.repository.StudyBlockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class StudyBlockServiceTest {

    @Mock
    private StudyBlockRepository studyBlockRepository;

    @Mock
    private ProblemRepository problemRepository;

    @InjectMocks
    private StudyBlockService studyBlockService;

    @Test
    void createStudyBlockShouldSaveStudyBlock() {

        CreateStudyBlockRequest request = new CreateStudyBlockRequest();

        request.setTitle("Title");
        request.setActive(false);

        StudyBlock savedStudyBlock = new StudyBlock(
                1L,
                "Title",
                false
        );

        when(studyBlockRepository.save(any(StudyBlock.class))).thenReturn(savedStudyBlock);

        StudyBlockResponse response = studyBlockService.createStudyBlock(request);

        assertEquals(1L, response.getId());
        assertEquals("Title", response.getTitle());
        assertFalse(response.isActive());

        ArgumentCaptor<StudyBlock> studyBlockCaptor = ArgumentCaptor.forClass(StudyBlock.class);

        verify(studyBlockRepository).save(studyBlockCaptor.capture());

        StudyBlock studyBlockToSave = studyBlockCaptor.getValue();

        assertNull(studyBlockToSave.getId());
        assertEquals("Title", studyBlockToSave.getTitle());
        assertFalse(studyBlockToSave.isActive());
    }

    @Test
    void getStudyBLockByIdShouldReturnStudyBlockWhenStudyBlockExists() {

        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                true
        );

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));

        StudyBlockResponse response = studyBlockService.getStudyBlockById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Title", response.getTitle());
        assertTrue(response.isActive());

        verify(studyBlockRepository).findById(1L);
    }

    @Test
    void getStudyBlockByIdShouldThrowExceptionWhenStudyBlockDoesNotExist() {
        when(studyBlockRepository.findById(99L)).thenReturn(Optional.empty());

        StudyBlockNotFoundException exception = assertThrows(
                StudyBlockNotFoundException.class,
                () -> studyBlockService.getStudyBlockById(99L)
        );

        assertEquals("Study block with id 99 was not found", exception.getMessage());

        verify(studyBlockRepository).findById(99L);
    }

    @Test
    void getStudyBlocksShouldReturnPagedStudyBlocksWhenNoFiltersAreProvided() {
        StudyBlock studyBlock1 = new StudyBlock(
                1L,
                "Title 1",
                true
        );

        StudyBlock studyBlock2 = new StudyBlock(
                2L,
                "Title 2",
                false
        );

        Pageable pageable = PageRequest.of(0, 2);

        Page<StudyBlock> page = new PageImpl<>(List.of(studyBlock1, studyBlock2), pageable, 5);

        when(studyBlockRepository.findAll(pageable)).thenReturn(page);

        PageResponse<StudyBlockResponse> response = studyBlockService.getStudyBlocks(null, pageable);

        assertEquals(2, response.getContent().size());

        StudyBlockResponse studyBlockResponse1 = response.getContent().getFirst();

        assertEquals(1L, studyBlockResponse1.getId());
        assertEquals("Title 1", studyBlockResponse1.getTitle());
        assertTrue(studyBlockResponse1.isActive());

        StudyBlockResponse studyBlockResponse2 = response.getContent().get(1);

        assertEquals(2L, studyBlockResponse2.getId());
        assertEquals("Title 2", studyBlockResponse2.getTitle());
        assertFalse(studyBlockResponse2.isActive());

        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());

        verify(studyBlockRepository).findAll(pageable);
        verify(studyBlockRepository, never()).findByActive(anyBoolean(), eq(pageable));
    }

    @Test
    void getStudyBlocksShouldReturnEmptyPageWhenNoStudyBlocksExist() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<StudyBlock> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(studyBlockRepository.findAll(pageable)).thenReturn(page);

        PageResponse<StudyBlockResponse> response = studyBlockService.getStudyBlocks(null, pageable);

        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());

        verify(studyBlockRepository).findAll(pageable);
        verify(studyBlockRepository, never()).findByActive(anyBoolean(), eq(pageable));

    }

    @Test
    void getStudyBlocksShouldReturnPagedStudyBlocksFilteringByActive() {
        StudyBlock studyBlock1 = new StudyBlock(
                1L,
                "Title 1",
                true
        );

        StudyBlock studyBlock2 = new StudyBlock(
                2L,
                "Title 2",
                true
        );

        Pageable pageable = PageRequest.of(0, 2);

        Page<StudyBlock> page = new PageImpl<>(List.of(studyBlock1, studyBlock2), pageable, 5);

        when(studyBlockRepository.findByActive(true, pageable)).thenReturn(page);

        PageResponse<StudyBlockResponse> response = studyBlockService.getStudyBlocks(true, pageable);

        assertEquals(2, response.getContent().size());

        StudyBlockResponse studyBlockResponse1 = response.getContent().getFirst();

        assertEquals(1L, studyBlockResponse1.getId());
        assertEquals("Title 1", studyBlockResponse1.getTitle());
        assertTrue(studyBlockResponse1.isActive());

        StudyBlockResponse studyBlockResponse2 = response.getContent().get(1);

        assertEquals(2L, studyBlockResponse2.getId());
        assertEquals("Title 2", studyBlockResponse2.getTitle());
        assertTrue(studyBlockResponse2.isActive());

        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());

        verify(studyBlockRepository).findByActive(true, pageable);
        verify(studyBlockRepository, never()).findAll(pageable);
    }

    @Test
    void updateStudyBlockShouldReturnUpdatedStudyBlockWhenStudyBlockExists() {
        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                true
        );

        UpdateStudyBlockRequest request = new UpdateStudyBlockRequest();

        request.setTitle("Updated Title");
        request.setActive(false);

        StudyBlock updatedStudyBlock = new StudyBlock(
                1L,
                "Updated Title",
                false
        );

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(studyBlockRepository.save(any(StudyBlock.class))).thenReturn(updatedStudyBlock);

        StudyBlockResponse studyBlockResponse = studyBlockService.updateStudyBlock(1L, request);

        assertEquals(1L, studyBlockResponse.getId());
        assertEquals("Updated Title", studyBlockResponse.getTitle());
        assertFalse(studyBlockResponse.isActive());

        ArgumentCaptor<StudyBlock> studyBlockCaptor = ArgumentCaptor.forClass(StudyBlock.class);

        verify(studyBlockRepository).findById(1L);
        verify(studyBlockRepository).save(studyBlockCaptor.capture());

        StudyBlock studyBlockToSave = studyBlockCaptor.getValue();

        assertEquals(1L, studyBlockToSave.getId());
        assertEquals("Updated Title", studyBlockToSave.getTitle());
        assertFalse(studyBlockToSave.isActive());
    }

    @Test
    void updateStudyBlockShouldThrowStudyBlockNotFoundExceptionWhenStudyBlockDoesNotExist() {
        UpdateStudyBlockRequest request = new UpdateStudyBlockRequest();

        request.setTitle("Updated Title");
        request.setActive(false);

        when(studyBlockRepository.findById(99L)).thenReturn(Optional.empty());

        StudyBlockNotFoundException exception = assertThrows(StudyBlockNotFoundException.class,
                () -> studyBlockService.updateStudyBlock(99L, request));

        assertEquals("Study block with id 99 was not found", exception.getMessage());

        verify(studyBlockRepository).findById(99L);
        verify(studyBlockRepository, never()).save(any(StudyBlock.class));
    }

    @Test
    void deleteStudyBlockShouldDeleteStudyBlockWhenStudyBlockExistsAndIsEmpty() {
        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                true
        );

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.existsByStudyBlockId(1L)).thenReturn(false);

        studyBlockService.deleteStudyBlock(1L);

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).existsByStudyBlockId(1L);
        verify(studyBlockRepository).delete(studyBlock);
    }

    @Test
    void deleteStudyBlockShouldThrowStudyBlockNotEmptyExceptionWhenStudyBlockExistsAndIsNotEmpty() {
        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                true
        );

        when(studyBlockRepository.findById(99L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.existsByStudyBlockId(99L)).thenReturn(true);

        StudyBlockNotEmptyException exception = assertThrows(StudyBlockNotEmptyException.class,
                () -> studyBlockService.deleteStudyBlock(99L));

        assertEquals("Study block must be empty to delete", exception.getMessage());

        verify(studyBlockRepository).findById(99L);
        verify(problemRepository).existsByStudyBlockId(99L);
        verify(studyBlockRepository, never()).delete(studyBlock);
    }


    @Test
    void deleteStudyBlockShouldThrowStudyBlockNotFoundExceptionWhenStudyBlockDoesNotExist() {
        when(studyBlockRepository.findById(99L)).thenReturn(Optional.empty());

        StudyBlockNotFoundException exception = assertThrows(StudyBlockNotFoundException.class,
                () -> studyBlockService.deleteStudyBlock(99L));

        assertEquals("Study block with id 99 was not found", exception.getMessage());

        verify(studyBlockRepository).findById(99L);
        verify(problemRepository, never()).existsByStudyBlockId(anyLong());
        verify(studyBlockRepository, never()).delete(any(StudyBlock.class));
    }
}
