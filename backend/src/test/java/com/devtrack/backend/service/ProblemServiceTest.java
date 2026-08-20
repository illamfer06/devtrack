package com.devtrack.backend.service;

import com.devtrack.backend.dto.CreateProblemRequest;
import com.devtrack.backend.dto.PageResponse;
import com.devtrack.backend.dto.ProblemResponse;
import com.devtrack.backend.dto.UpdateProblemRequest;
import com.devtrack.backend.exception.ProblemNotFoundException;
import com.devtrack.backend.model.Difficulty;
import com.devtrack.backend.model.Problem;
import com.devtrack.backend.repository.ProblemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @InjectMocks
    private ProblemService problemService;

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

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));

        ProblemResponse problemResponse = problemService.getProblemById(1L);

        assertEquals(1L, problemResponse.getId());
        assertEquals("Title", problemResponse.getTitle());
        assertEquals(Difficulty.EASY, problemResponse.getDifficulty());
        assertEquals("Algorithm", problemResponse.getAlgorithm());
        assertTrue(problemResponse.isSolved());
        assertEquals("Notes", problemResponse.getNotes());
        assertEquals("Url", problemResponse.getUrl());

        verify(problemRepository).findById(1L);
    }

    @Test
    void getProblemByIdShouldThrowExceptionWhenProblemDoesNotExist() {

        when(problemRepository.findById(99L)).thenReturn(Optional.empty());

        ProblemNotFoundException exception = assertThrows(
                ProblemNotFoundException.class,
                () -> problemService.getProblemById(99L)
        );

        assertEquals("Problem with id 99 was not found", exception.getMessage());

        verify(problemRepository).findById(99L);
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

        Pageable pageable = PageRequest.of(0, 2);

        Page<Problem> page = new PageImpl<>(problems, pageable, 5);

        when(problemRepository.findAll(pageable)).thenReturn(page);

        PageResponse<ProblemResponse> response = problemService.getProblems(null, null, pageable);

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

        verify(problemRepository).findAll(pageable);
        verify(problemRepository, never()).findByDifficulty(any(Difficulty.class), eq(pageable));
        verify(problemRepository, never()).findBySolved(anyBoolean(), eq(pageable));
        verify(problemRepository, never()).findByDifficultyAndSolved(any(Difficulty.class), anyBoolean(), eq(pageable));
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

        Pageable pageable = PageRequest.of(0, 2);

        Page<Problem> page = new PageImpl<>(problems, pageable, 5);

        when(problemRepository.findByDifficulty(Difficulty.EASY, pageable)).thenReturn(page);

        PageResponse<ProblemResponse> response = problemService.getProblems(Difficulty.EASY, null, pageable);

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

        verify(problemRepository).findByDifficulty(eq(Difficulty.EASY), eq(pageable));
        verify(problemRepository, never()).findAll(pageable);
        verify(problemRepository, never()).findBySolved(anyBoolean(), eq(pageable));
        verify(problemRepository, never()).findByDifficultyAndSolved(any(Difficulty.class), anyBoolean(), eq(pageable));
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

        Pageable pageable = PageRequest.of(0, 2);

        Page<Problem> page = new PageImpl<>(problems, pageable, 5);

        when(problemRepository.findBySolved(true, pageable)).thenReturn(page);

        PageResponse<ProblemResponse> response = problemService.getProblems(null, true, pageable);

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

        verify(problemRepository).findBySolved(eq(true), eq(pageable));
        verify(problemRepository, never()).findAll(pageable);
        verify(problemRepository, never()).findByDifficulty(any(Difficulty.class), eq(pageable));
        verify(problemRepository, never()).findByDifficultyAndSolved(any(Difficulty.class), anyBoolean(), eq(pageable));
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

        Pageable pageable = PageRequest.of(0, 2);

        Page<Problem> page = new PageImpl<>(problems, pageable, 5);

        when(problemRepository.findByDifficultyAndSolved(Difficulty.EASY, true, pageable)).thenReturn(page);

        PageResponse<ProblemResponse> response = problemService.getProblems(Difficulty.EASY, true, pageable);

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

        verify(problemRepository).findByDifficultyAndSolved(eq(Difficulty.EASY), eq(true), eq(pageable));
        verify(problemRepository, never()).findAll(pageable);
        verify(problemRepository, never()).findByDifficulty(any(Difficulty.class), eq(pageable));
        verify(problemRepository, never()).findBySolved(anyBoolean(), eq(pageable));
    }

    @Test
    void createProblemShouldSaveProblem() {

        CreateProblemRequest request = new CreateProblemRequest();

        request.setTitle("Title");
        request.setDifficulty(Difficulty.EASY);
        request.setAlgorithm("Algorithm");
        request.setSolved(true);
        request.setNotes("Notes");
        request.setUrl("Url");

        Problem savedProblem = new Problem (
                1L,
                "Title",
                Difficulty.EASY,
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);

        ProblemResponse problemResponse = problemService.createProblem(request);

        assertEquals(1L, problemResponse.getId());
        assertEquals("Title", problemResponse.getTitle());
        assertEquals(Difficulty.EASY, problemResponse.getDifficulty());
        assertEquals("Algorithm", problemResponse.getAlgorithm());
        assertTrue(problemResponse.isSolved());
        assertEquals("Notes", problemResponse.getNotes());
        assertEquals("Url", problemResponse.getUrl());

        ArgumentCaptor<Problem> problemCaptor = ArgumentCaptor.forClass(Problem.class);

        verify(problemRepository).save(problemCaptor.capture());

        Problem problemToSave = problemCaptor.getValue();

        assertNull(problemToSave.getId());
        assertEquals("Title", problemToSave.getTitle());
        assertEquals(Difficulty.EASY, problemToSave.getDifficulty());
        assertEquals("Algorithm", problemToSave.getAlgorithm());
        assertTrue(problemToSave.isSolved());
        assertEquals("Notes", problemToSave.getNotes());
        assertEquals("Url", problemToSave.getUrl());
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

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any(Problem.class))).thenReturn(updatedProblem);

        ProblemResponse problemResponse = problemService.updateProblem(1L, request);

        assertEquals(1L, problemResponse.getId());
        assertEquals("Updated Title", problemResponse.getTitle());
        assertEquals(Difficulty.MEDIUM, problemResponse.getDifficulty());
        assertEquals("Updated Algorithm", problemResponse.getAlgorithm());
        assertTrue(problemResponse.isSolved());
        assertEquals("Updated Notes", problemResponse.getNotes());
        assertEquals("Updated Url", problemResponse.getUrl());

        ArgumentCaptor<Problem> problemCaptor = ArgumentCaptor.forClass(Problem.class);

        verify(problemRepository).findById(1L);
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
    void updateProblemShouldThrowProblemNotFoundExceptionWhenProblemDoesNotExist() {
        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("Updated Title");
        request.setDifficulty(Difficulty.HARD);
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(true);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");

        when(problemRepository.findById(99L)).thenReturn(Optional.empty());

        ProblemNotFoundException exception = assertThrows(ProblemNotFoundException.class,
                () -> problemService.updateProblem(99L, request));

        assertEquals("Problem with id 99 was not found", exception.getMessage());

        verify(problemRepository).findById(99L);
        verify(problemRepository, never()).save(any(Problem.class));
    }

    @Test
    void deleteProblemShouldDeleteProblemWhenProblemExists() {
        Problem problem = new Problem(
                1L,
                "Title",
                Difficulty.EASY,
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));

        problemService.deleteProblem(1L);

        verify(problemRepository).findById(1L);
        verify(problemRepository).delete(problem);
    }

    @Test
    void deleteProblemShouldThrowProblemNotFoundExceptionWhenProblemDoesNotExist() {
        when(problemRepository.findById(99L)).thenReturn(Optional.empty());

        ProblemNotFoundException exception = assertThrows(ProblemNotFoundException.class,
                () -> problemService.deleteProblem(99L));

        assertEquals("Problem with id 99 was not found", exception.getMessage());

        verify(problemRepository).findById(99L);
        verify(problemRepository, never()).delete(any(Problem.class));
    }
}