package com.devtrack.backend.service;

import com.devtrack.backend.dto.*;
import com.devtrack.backend.exception.ProblemNotFoundException;
import com.devtrack.backend.exception.StudyBlockNotFoundException;
import com.devtrack.backend.model.Difficulty;
import com.devtrack.backend.model.Problem;
import com.devtrack.backend.model.StudyBlock;
import com.devtrack.backend.repository.ProblemRepository;
import com.devtrack.backend.repository.StudyBlockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private StudyBlockRepository studyBlockRepository;

    @InjectMocks
    private ProblemService problemService;

    @Test
    void createProblemShouldSaveProblemWhenStudyBlockExists() {

        CreateProblemRequest request = new CreateProblemRequest();

        request.setTitle("Title");
        request.setDifficulty(Difficulty.EASY);
        request.setAlgorithm("Algorithm");
        request.setSolved(true);
        request.setNotes("Notes");
        request.setUrl("Url");

        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                false
        );

        Problem savedProblem = new Problem (
                1L,
                "Title",
                Difficulty.EASY,
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);

        ProblemResponse problemResponse = problemService.createProblem(1L, request);

        assertEquals(1L, problemResponse.getId());
        assertEquals("Title", problemResponse.getTitle());
        assertEquals(Difficulty.EASY, problemResponse.getDifficulty());
        assertEquals("Algorithm", problemResponse.getAlgorithm());
        assertTrue(problemResponse.isSolved());
        assertEquals("Notes", problemResponse.getNotes());
        assertEquals("Url", problemResponse.getUrl());

        ArgumentCaptor<Problem> problemCaptor = ArgumentCaptor.forClass(Problem.class);

        verify(problemRepository).save(problemCaptor.capture());
        verify(studyBlockRepository).findById(1L);

        Problem problemToSave = problemCaptor.getValue();

        assertNull(problemToSave.getId());
        assertEquals("Title", problemToSave.getTitle());
        assertEquals(Difficulty.EASY, problemToSave.getDifficulty());
        assertEquals("Algorithm", problemToSave.getAlgorithm());
        assertTrue(problemToSave.isSolved());
        assertEquals("Notes", problemToSave.getNotes());
        assertEquals("Url", problemToSave.getUrl());
        assertEquals(1L, problemToSave.getStudyBlock().getId());
    }

    @Test
    void createProblemShouldThrowStudyBlockNotFoundExceptionWhenStudyBlockDoesNotExist() {

        CreateProblemRequest request = new CreateProblemRequest();

        request.setTitle("Title");
        request.setDifficulty(Difficulty.EASY);
        request.setAlgorithm("Algorithm");
        request.setSolved(true);
        request.setNotes("Notes");
        request.setUrl("Url");

        when(studyBlockRepository.findById(99L)).thenReturn(Optional.empty());

        StudyBlockNotFoundException exception = assertThrows(StudyBlockNotFoundException.class,
                () -> problemService.createProblem(99L, request));

        assertEquals("Study block with id 99 was not found",
                exception.getMessage());

        verify(studyBlockRepository).findById(99L);
        verify(problemRepository, never()).save(any(Problem.class));
    }

    @Test
    void getProblemsShouldReturnPagedProblemsWhenNoFiltersAreProvided() {
        List<Problem> problems = new ArrayList<>();

        problems.add(new Problem(
                1L,
                "Title 1",
                Difficulty.EASY,
                "Algorithm 1",
                true,
                "Notes 1",
                "Url 1"
        ));

        problems.add(new Problem(
                2L,
                "Title 2",
                Difficulty.HARD,
                "Algorithm 2",
                false,
                "Notes 2",
                "Url 2"
        ));

        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                true
        );

        Pageable pageable = PageRequest.of(0, 2);

        Page<Problem> page = new PageImpl<>(problems, pageable, 5);

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByStudyBlockId(1L, pageable)).thenReturn(page);

        PageResponse<ProblemResponse> response = problemService.getProblems(1L,null, null, pageable);

        assertEquals(2, response.getContent().size());

        ProblemResponse problemResponse1 = response.getContent().getFirst();

        assertEquals(1L, problemResponse1.getId());
        assertEquals("Title 1", problemResponse1.getTitle());
        assertEquals(Difficulty.EASY, problemResponse1.getDifficulty());
        assertEquals("Algorithm 1", problemResponse1.getAlgorithm());
        assertTrue(problemResponse1.isSolved());
        assertEquals("Notes 1", problemResponse1.getNotes());
        assertEquals("Url 1", problemResponse1.getUrl());

        ProblemResponse problemResponse2 = response.getContent().get(1);

        assertEquals(2L, problemResponse2.getId());
        assertEquals("Title 2", problemResponse2.getTitle());
        assertEquals(Difficulty.HARD, problemResponse2.getDifficulty());
        assertEquals("Algorithm 2", problemResponse2.getAlgorithm());
        assertFalse(problemResponse2.isSolved());
        assertEquals("Notes 2", problemResponse2.getNotes());
        assertEquals("Url 2", problemResponse2.getUrl());

        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).findByStudyBlockId(1L, pageable);
        verify(problemRepository, never()).findByStudyBlockIdAndDifficulty(eq(1L), any(Difficulty.class), eq(pageable));
        verify(problemRepository, never()).findByStudyBlockIdAndSolved(eq(1L), anyBoolean(), eq(pageable));
        verify(problemRepository, never()).findByStudyBlockIdAndDifficultyAndSolved(eq(1L), any(Difficulty.class), anyBoolean(), eq(pageable));
    }

    @Test
    void getProblemsShouldThrowStudyBlockNotFoundExceptionWhenStudyBlockDoesNotExist() {

        Pageable pageable = PageRequest.of(0, 2);

        when(studyBlockRepository.findById(99L)).thenReturn(Optional.empty());

        StudyBlockNotFoundException exception = assertThrows(StudyBlockNotFoundException.class,
                () -> problemService.getProblems(99L, null, null, pageable));

        assertEquals("Study block with id 99 was not found", exception.getMessage());

        verify(studyBlockRepository).findById(99L);
        verifyNoInteractions(problemRepository);
    }

    @Test
    void getProblemsShouldReturnEmptyPageWhenNoProblemsExist() {
        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                false
        );

        Pageable pageable = PageRequest.of(0, 2);

        Page<Problem> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByStudyBlockId(1L, pageable)).thenReturn(page);

        PageResponse<ProblemResponse> response = problemService.getProblems(1L,null, null, pageable);

        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).findByStudyBlockId(1L, pageable);
        verify(problemRepository, never()).findByStudyBlockIdAndDifficulty(eq(1L), any(Difficulty.class), eq(pageable));
        verify(problemRepository, never()).findByStudyBlockIdAndSolved(eq(1L), anyBoolean(), eq(pageable));
        verify(problemRepository, never()).findByStudyBlockIdAndDifficultyAndSolved(eq(1L), any(Difficulty.class), anyBoolean(), eq(pageable));
    }

    @Test
    void getProblemsShouldReturnPagedProblemsFilteringByDifficulty() {
        List<Problem> problems = new ArrayList<>();

        problems.add(new Problem(
                1L,
                "Title 1",
                Difficulty.EASY,
                "Algorithm 1",
                true,
                "Notes 1",
                "Url 1"
        ));

        problems.add(new Problem(
                2L,
                "Title 2",
                Difficulty.EASY,
                "Algorithm 2",
                false,
                "Notes 2",
                "Url 2"
        ));

        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                false
        );

        Pageable pageable = PageRequest.of(0, 2);

        Page<Problem> page = new PageImpl<>(problems, pageable, 5);

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByStudyBlockIdAndDifficulty(1L, Difficulty.EASY, pageable)).thenReturn(page);

        PageResponse<ProblemResponse> response = problemService.getProblems(1L, Difficulty.EASY, null, pageable);

        assertEquals(2, response.getContent().size());

        ProblemResponse problemResponse1 = response.getContent().getFirst();

        assertEquals(1L, problemResponse1.getId());
        assertEquals("Title 1", problemResponse1.getTitle());
        assertEquals(Difficulty.EASY, problemResponse1.getDifficulty());
        assertEquals("Algorithm 1", problemResponse1.getAlgorithm());
        assertTrue(problemResponse1.isSolved());
        assertEquals("Notes 1", problemResponse1.getNotes());
        assertEquals("Url 1", problemResponse1.getUrl());

        ProblemResponse problemResponse2 = response.getContent().get(1);

        assertEquals(2L, problemResponse2.getId());
        assertEquals("Title 2", problemResponse2.getTitle());
        assertEquals(Difficulty.EASY, problemResponse2.getDifficulty());
        assertEquals("Algorithm 2", problemResponse2.getAlgorithm());
        assertFalse(problemResponse2.isSolved());
        assertEquals("Notes 2", problemResponse2.getNotes());
        assertEquals("Url 2", problemResponse2.getUrl());

        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).findByStudyBlockIdAndDifficulty(eq(1L), eq(Difficulty.EASY), eq(pageable));
        verify(problemRepository, never()).findByStudyBlockId(eq(1L), eq(pageable));
        verify(problemRepository, never()).findByStudyBlockIdAndSolved(eq(1L), anyBoolean(), eq(pageable));
        verify(problemRepository, never()).findByStudyBlockIdAndDifficultyAndSolved(eq(1L), any(Difficulty.class), anyBoolean(), eq(pageable));
    }

    @Test
    void getProblemsShouldReturnPagedProblemsFilteringBySolved() {
        List<Problem> problems = new ArrayList<>();

        problems.add(new Problem(
                1L,
                "Title 1",
                Difficulty.EASY,
                "Algorithm 1",
                true,
                "Notes 1",
                "Url 1"
        ));

        problems.add(new Problem(
                2L,
                "Title 2",
                Difficulty.EASY,
                "Algorithm 2",
                true,
                "Notes 2",
                "Url 2"
        ));

        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                false
        );

        Pageable pageable = PageRequest.of(0, 2);

        Page<Problem> page = new PageImpl<>(problems, pageable, 5);

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByStudyBlockIdAndSolved(1L, true, pageable)).thenReturn(page);

        PageResponse<ProblemResponse> response = problemService.getProblems(1L,null, true, pageable);

        assertEquals(2, response.getContent().size());

        ProblemResponse problemResponse1 = response.getContent().getFirst();

        assertEquals(1L, problemResponse1.getId());
        assertEquals("Title 1", problemResponse1.getTitle());
        assertEquals(Difficulty.EASY, problemResponse1.getDifficulty());
        assertEquals("Algorithm 1", problemResponse1.getAlgorithm());
        assertTrue(problemResponse1.isSolved());
        assertEquals("Notes 1", problemResponse1.getNotes());
        assertEquals("Url 1", problemResponse1.getUrl());

        ProblemResponse problemResponse2 = response.getContent().get(1);

        assertEquals(2L, problemResponse2.getId());
        assertEquals("Title 2", problemResponse2.getTitle());
        assertEquals(Difficulty.EASY, problemResponse2.getDifficulty());
        assertEquals("Algorithm 2", problemResponse2.getAlgorithm());
        assertTrue(problemResponse2.isSolved());
        assertEquals("Notes 2", problemResponse2.getNotes());
        assertEquals("Url 2", problemResponse2.getUrl());

        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).findByStudyBlockIdAndSolved(1L, true, pageable);
        verify(problemRepository, never()).findByStudyBlockId(1L, pageable);
        verify(problemRepository, never()).findByStudyBlockIdAndDifficulty(eq(1L), any(Difficulty.class), eq(pageable));
        verify(problemRepository, never()).findByStudyBlockIdAndDifficultyAndSolved(eq(1L), any(Difficulty.class), anyBoolean(), eq(pageable));
    }

    @Test
    void getProblemsShouldReturnPagedProblemsFilteringByDifficultyAndSolved() {
        List<Problem> problems = new ArrayList<>();

        problems.add(new Problem(
                1L,
                "Title 1",
                Difficulty.EASY,
                "Algorithm 1",
                true,
                "Notes 1",
                "Url 1"
        ));

        problems.add(new Problem(
                2L,
                "Title 2",
                Difficulty.EASY,
                "Algorithm 2",
                true,
                "Notes 2",
                "Url 2"
        ));

        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                false
        );

        Pageable pageable = PageRequest.of(0, 2);

        Page<Problem> page = new PageImpl<>(problems, pageable, 5);

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByStudyBlockIdAndDifficultyAndSolved(1L, Difficulty.EASY, true, pageable)).thenReturn(page);

        PageResponse<ProblemResponse> response = problemService.getProblems(1L, Difficulty.EASY, true, pageable);

        assertEquals(2, response.getContent().size());

        ProblemResponse problemResponse1 = response.getContent().getFirst();

        assertEquals(1L, problemResponse1.getId());
        assertEquals("Title 1", problemResponse1.getTitle());
        assertEquals(Difficulty.EASY, problemResponse1.getDifficulty());
        assertEquals("Algorithm 1", problemResponse1.getAlgorithm());
        assertTrue(problemResponse1.isSolved());
        assertEquals("Notes 1", problemResponse1.getNotes());
        assertEquals("Url 1", problemResponse1.getUrl());

        ProblemResponse problemResponse2 = response.getContent().get(1);

        assertEquals(2L, problemResponse2.getId());
        assertEquals("Title 2", problemResponse2.getTitle());
        assertEquals(Difficulty.EASY, problemResponse2.getDifficulty());
        assertEquals("Algorithm 2", problemResponse2.getAlgorithm());
        assertTrue(problemResponse2.isSolved());
        assertEquals("Notes 2", problemResponse2.getNotes());
        assertEquals("Url 2", problemResponse2.getUrl());

        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).findByStudyBlockIdAndDifficultyAndSolved(1L, Difficulty.EASY, true, pageable);
        verify(problemRepository, never()).findByStudyBlockId(1L, pageable);
        verify(problemRepository, never()).findByStudyBlockIdAndDifficulty(eq(1L), any(Difficulty.class), eq(pageable));
        verify(problemRepository, never()).findByStudyBlockIdAndSolved(eq(1L), anyBoolean(), eq(pageable));
    }

    @Test
    void getProblemByIdShouldReturnProblemWhenProblemExists() {
        Problem problem = new Problem(
                1L,
                "Title",
                Difficulty.EASY,
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                true
        );

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByIdAndStudyBlockId(1L, 1L)).thenReturn(Optional.of(problem));

        ProblemResponse problemResponse = problemService.getProblemById(1L, 1L);

        assertEquals(1L, problemResponse.getId());
        assertEquals("Title", problemResponse.getTitle());
        assertEquals(Difficulty.EASY, problemResponse.getDifficulty());
        assertEquals("Algorithm", problemResponse.getAlgorithm());
        assertTrue(problemResponse.isSolved());
        assertEquals("Notes", problemResponse.getNotes());
        assertEquals("Url", problemResponse.getUrl());

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).findByIdAndStudyBlockId(1L, 1L);
    }

    @Test
    void getProblemByIdShouldThrowExceptionWhenStudyBlockDoesNotExist() {

        when(studyBlockRepository.findById(99L)).thenReturn(Optional.empty());

        StudyBlockNotFoundException exception = assertThrows(
                StudyBlockNotFoundException.class,
                () -> problemService.getProblemById(1L, 99L)
        );

        assertEquals("Study block with id 99 was not found", exception.getMessage());

        verify(studyBlockRepository).findById(99L);
        verify(problemRepository, never()).findByIdAndStudyBlockId(anyLong(),anyLong());
    }

    @Test
    void getProblemByIdShouldThrowExceptionWhenProblemDoesNotExist() {
        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                false
        );

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByIdAndStudyBlockId(99L, 1L)).thenReturn(Optional.empty());

        ProblemNotFoundException exception = assertThrows(
                ProblemNotFoundException.class,
                () -> problemService.getProblemById(99L, 1L)
        );

        assertEquals("Problem with id 99 was not found in study block 1", exception.getMessage());

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).findByIdAndStudyBlockId(99L, 1L);
    }

    @Test
    void updateProblemShouldReturnUpdatedProblemWhenProblemExists() {
        Problem problem = new Problem(
                1L,
                "Title",
                Difficulty.EASY,
                "Algorithm",
                false,
                "Notes",
                "Url"
        );

        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("Updated Title");
        request.setDifficulty(Difficulty.MEDIUM);
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(true);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");

        Problem updatedProblem = new Problem(
                1L,
                "Updated Title",
                Difficulty.MEDIUM,
                "Updated Algorithm",
                true,
                "Updated Notes",
                "Updated Url"
        );

        StudyBlock studyBlock = new StudyBlock(
                5L,
                "Title",
                false
        );

        when(studyBlockRepository.findById(5L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByIdAndStudyBlockId(1L, 5L)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any(Problem.class))).thenReturn(updatedProblem);

        ProblemResponse problemResponse = problemService.updateProblem(1L, 5L, request);

        assertEquals(1L, problemResponse.getId());
        assertEquals("Updated Title", problemResponse.getTitle());
        assertEquals(Difficulty.MEDIUM, problemResponse.getDifficulty());
        assertEquals("Updated Algorithm", problemResponse.getAlgorithm());
        assertTrue(problemResponse.isSolved());
        assertEquals("Updated Notes", problemResponse.getNotes());
        assertEquals("Updated Url", problemResponse.getUrl());

        ArgumentCaptor<Problem> problemCaptor = ArgumentCaptor.forClass(Problem.class);

        verify(studyBlockRepository).findById(5L);
        verify(problemRepository).findByIdAndStudyBlockId(1L, 5L);
        verify(problemRepository).save(problemCaptor.capture());

        Problem problemToSave = problemCaptor.getValue();

        assertEquals(1L, problemToSave.getId());
        assertEquals("Updated Title", problemToSave.getTitle());
        assertEquals(Difficulty.MEDIUM, problemToSave.getDifficulty());
        assertEquals("Updated Algorithm", problemToSave.getAlgorithm());
        assertTrue(problemToSave.isSolved());
        assertEquals("Updated Notes", problemToSave.getNotes());
        assertEquals("Updated Url", problemToSave.getUrl());
    }

    @Test
    void updateProblemShouldThrowStudyBlockNotFoundExceptionWhenStudyBlockDoesNotExist() {
        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("Updated Title");
        request.setDifficulty(Difficulty.HARD);
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(true);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");

        when(studyBlockRepository.findById(99L)).thenReturn(Optional.empty());

        StudyBlockNotFoundException exception = assertThrows(StudyBlockNotFoundException.class,
                () -> problemService.updateProblem(1L, 99L, request));

        assertEquals("Study block with id 99 was not found", exception.getMessage());

        verify(studyBlockRepository).findById(99L);
        verify(problemRepository, never()).findByIdAndStudyBlockId(1L, 99L);
        verify(problemRepository, never()).save(any(Problem.class));
    }

    @Test
    void updateProblemShouldThrowProblemNotFoundExceptionWhenProblemDoesNotExist() {
        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("Updated Title");
        request.setDifficulty(Difficulty.HARD);
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(true);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");

        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                false
        );

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByIdAndStudyBlockId(99L, 1L)).thenReturn(Optional.empty());

        ProblemNotFoundException exception = assertThrows(ProblemNotFoundException.class,
                () -> problemService.updateProblem(99L, 1L, request));

        assertEquals("Problem with id 99 was not found in study block 1", exception.getMessage());

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).findByIdAndStudyBlockId(99L, 1L);
        verify(problemRepository, never()).save(any(Problem.class));
    }

    @Test
    void deleteProblemShouldDeleteProblemWhenProblemExists() {
        StudyBlock studyBlock = new StudyBlock(
                5L,
                "Title",
                false
        );

        Problem problem = new Problem(
                1L,
                "Title",
                Difficulty.EASY,
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        when(studyBlockRepository.findById(5L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByIdAndStudyBlockId(1L, 5L)).thenReturn(Optional.of(problem));

        problemService.deleteProblem(1L, 5L);

        verify(studyBlockRepository).findById(5L);
        verify(problemRepository).findByIdAndStudyBlockId(1L, 5L);
        verify(problemRepository).delete(problem);
    }

    @Test
    void deleteProblemShouldThrowStudyBlockNotFoundExceptionWhenStudyBlockDoesNotExist() {

        when(studyBlockRepository.findById(99L)).thenReturn(Optional.empty());

        StudyBlockNotFoundException exception = assertThrows(StudyBlockNotFoundException.class,
                () -> problemService.deleteProblem(1L, 99L));

        assertEquals("Study block with id 99 was not found", exception.getMessage());

        verify(studyBlockRepository).findById(99L);
        verify(problemRepository, never()).findByIdAndStudyBlockId(1L, 99L);
        verify(problemRepository, never()).delete(any(Problem.class));
    }

    @Test
    void deleteProblemShouldThrowProblemNotFoundExceptionWhenProblemDoesNotExist() {
        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                false
        );

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByIdAndStudyBlockId(99L, 1L)).thenReturn(Optional.empty());

        ProblemNotFoundException exception = assertThrows(ProblemNotFoundException.class,
                () -> problemService.deleteProblem(99L, 1L));

        assertEquals("Problem with id 99 was not found in study block 1", exception.getMessage());

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).findByIdAndStudyBlockId(99L, 1L);
        verify(problemRepository, never()).delete(any(Problem.class));
    }

    @Test
    void bulkDeleteProblemsShouldDeleteAllSelectedProblems() {
        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                false
        );

        Problem problem1 = new Problem(
                1L,
                "Title",
                Difficulty.EASY,
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        Problem problem2 = new Problem(
                2L,
                "Title",
                Difficulty.EASY,
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        List<Problem> problems = List.of(problem1, problem2);

        BulkDeleteProblemsRequest request = new BulkDeleteProblemsRequest();

        Set<Long> problemIds = Set.of(1L,2L);

        request.setProblemIds(problemIds);

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByIdInAndStudyBlockId(problemIds,1L)).thenReturn(problems);

        problemService.bulkDeleteProblems(1L, request);

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).findByIdInAndStudyBlockId(problemIds, 1L);
        verify(problemRepository).deleteAllInBatch(problems);
    }

    @Test
    void bulkDeleteProblemsShouldThrowStudyBlockNotFoundExceptionWhenStudyBlockIdDoesNotExist() {

        BulkDeleteProblemsRequest request = new BulkDeleteProblemsRequest();

        Set<Long> problemIds = Set.of(1L,2L);

        request.setProblemIds(problemIds);

        when(studyBlockRepository.findById(99L)).thenReturn(Optional.empty());

        StudyBlockNotFoundException exception = assertThrows(StudyBlockNotFoundException.class,
                () -> problemService.bulkDeleteProblems(99L, request));

        assertEquals("Study block with id 99 was not found", exception.getMessage());

        verify(studyBlockRepository).findById(99L);
        verify(problemRepository, never()).findByIdInAndStudyBlockId(anySet(), eq(99L));
        verify(problemRepository, never()).deleteAllInBatch(anyList());
    }

    @Test
    void bulkDeleteProblemsShouldThrowProblemNotFoundExceptionWhenAProblemIsNotInStudyBlock() {
        StudyBlock studyBlock = new StudyBlock(
                1L,
                "Title",
                false
        );

        Problem problem2 = new Problem(
                2L,
                "Title",
                Difficulty.EASY,
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        BulkDeleteProblemsRequest request = new BulkDeleteProblemsRequest();

        Set<Long> problemIds = Set.of(99L,2L);

        request.setProblemIds(problemIds);

        when(studyBlockRepository.findById(1L)).thenReturn(Optional.of(studyBlock));
        when(problemRepository.findByIdInAndStudyBlockId(problemIds, 1L)).thenReturn(List.of(problem2));

        ProblemNotFoundException exception = assertThrows(ProblemNotFoundException.class,
                () -> problemService.bulkDeleteProblems(1L, request));

        assertEquals("One or more selected problems were not found in study block 1", exception.getMessage());

        verify(studyBlockRepository).findById(1L);
        verify(problemRepository).findByIdInAndStudyBlockId(problemIds, 1L);
        verify(problemRepository, never()).deleteAllInBatch(anyList());
    }
}