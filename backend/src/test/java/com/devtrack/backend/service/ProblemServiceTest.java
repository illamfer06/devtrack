package com.devtrack.backend.service;

import com.devtrack.backend.dto.CreateProblemRequest;
import com.devtrack.backend.dto.ProblemResponse;
import com.devtrack.backend.dto.UpdateProblemRequest;
import com.devtrack.backend.exception.ProblemNotFoundException;
import com.devtrack.backend.model.Problem;
import com.devtrack.backend.repository.ProblemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
                "Difficulty",
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));

        ProblemResponse problemResponse = problemService.getProblemById(1L);

        assertEquals(1L, problemResponse.getId());
        assertEquals("Title", problemResponse.getTitle());
        assertEquals("Difficulty", problemResponse.getDifficulty());
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
    void getAllProblemsShouldReturnAllProblems() {
        List<Problem> problems = new ArrayList<>();

        problems.add(new Problem(
                1L,
                "Title1",
                "Difficulty1",
                "Algorithm1",
                true,
                "Notes1",
                "Url1"
        ));

        problems.add(new Problem(
                2L,
                "Title2",
                "Difficulty2",
                "Algorithm2",
                false,
                "Notes2",
                 "Url2"
        ));

        when(problemRepository.findAll(Sort.by("id"))).thenReturn(problems);

        List<ProblemResponse> problemResponses = problemService.getAllProblems();

        assertEquals(2, problemResponses.size());

        ProblemResponse problemResponse1 = problemResponses.getFirst();

        assertEquals(1L, problemResponse1.getId());
        assertEquals("Title1", problemResponse1.getTitle());
        assertEquals("Difficulty1", problemResponse1.getDifficulty());
        assertEquals("Algorithm1", problemResponse1.getAlgorithm());
        assertTrue(problemResponse1.isSolved());
        assertEquals("Notes1", problemResponse1.getNotes());
        assertEquals("Url1", problemResponse1.getUrl());

        ProblemResponse problemResponse2 = problemResponses.get(1);

        assertEquals(2L, problemResponse2.getId());
        assertEquals("Title2", problemResponse2.getTitle());
        assertEquals("Difficulty2", problemResponse2.getDifficulty());
        assertEquals("Algorithm2", problemResponse2.getAlgorithm());
        assertFalse(problemResponse2.isSolved());
        assertEquals("Notes2", problemResponse2.getNotes());
        assertEquals("Url2", problemResponse2.getUrl());

        verify(problemRepository).findAll(Sort.by("id"));
    }

    @Test
    void getAllProblemsShouldReturnEmptyListWhenRepositoryIsEmpty() {

        when(problemRepository.findAll(Sort.by("id"))).thenReturn(Collections.emptyList());

        List<ProblemResponse> problemResponses = problemService.getAllProblems();

        assertTrue(problemResponses.isEmpty());

        verify(problemRepository).findAll(Sort.by("id"));
    }

    @Test
    void createProblemShouldSaveProblem() {

        CreateProblemRequest request = new CreateProblemRequest();

        request.setTitle("Title");
        request.setDifficulty("Difficulty");
        request.setAlgorithm("Algorithm");
        request.setSolved(true);
        request.setNotes("Notes");
        request.setUrl("Url");

        Problem savedProblem = new Problem (
                1L,
                "Title",
                "Difficulty",
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);

        ProblemResponse problemResponse = problemService.createProblem(request);

        assertEquals(1L, problemResponse.getId());
        assertEquals("Title", problemResponse.getTitle());
        assertEquals("Difficulty", problemResponse.getDifficulty());
        assertEquals("Algorithm", problemResponse.getAlgorithm());
        assertTrue(problemResponse.isSolved());
        assertEquals("Notes", problemResponse.getNotes());
        assertEquals("Url", problemResponse.getUrl());

        ArgumentCaptor<Problem> problemCaptor = ArgumentCaptor.forClass(Problem.class);

        verify(problemRepository).save(problemCaptor.capture());

        Problem problemToSave = problemCaptor.getValue();

        assertNull(problemToSave.getId());
        assertEquals("Title", problemToSave.getTitle());
        assertEquals("Difficulty", problemToSave.getDifficulty());
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
                "Difficulty",
                "Algorithm",
                false,
                "Notes",
                "Url"
        );

        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("Updated Title");
        request.setDifficulty("Updated Difficulty");
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(true);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");

        Problem updatedProblem = new Problem(
                1L,
                "Updated Title",
                "Updated Difficulty",
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
        assertEquals("Updated Difficulty", problemResponse.getDifficulty());
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
        assertEquals("Updated Difficulty", problemToSave.getDifficulty());
        assertEquals("Updated Algorithm", problemToSave.getAlgorithm());
        assertTrue(problemToSave.isSolved());
        assertEquals("Updated Notes", problemToSave.getNotes());
        assertEquals("Updated Url", problemToSave.getUrl());
    }

    @Test
    void updateProblemShouldThrowProblemNotFoundExceptionWhenProblemDoesNotExist() {
        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("Updated Title");
        request.setDifficulty("Updated Difficulty");
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
                "Difficulty",
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
