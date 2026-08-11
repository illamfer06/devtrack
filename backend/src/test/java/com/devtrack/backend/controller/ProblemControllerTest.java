package com.devtrack.backend.controller;

import com.devtrack.backend.dto.CreateProblemRequest;
import com.devtrack.backend.dto.ProblemResponse;
import com.devtrack.backend.dto.UpdateProblemRequest;
import com.devtrack.backend.exception.ProblemNotFoundException;
import com.devtrack.backend.service.ProblemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProblemController.class)
class ProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProblemService problemService;

    @Test
    void getProblemsShouldReturn200WhenProblemsExist() throws Exception {
        ProblemResponse problemResponse1 = new ProblemResponse(
                1L,
                "Title 1",
                "Difficulty 1",
                "Algorithm 1",
                true,
                "Notes 1",
                "Url 1"
        );

        ProblemResponse problemResponse2 = new ProblemResponse(
                2L,
                "Title 2",
                "Difficulty 2",
                "Algorithm 2",
                false,
                "Notes 2",
                "Url 2"
        );

        when(problemService.getAllProblems()).thenReturn(List.of(problemResponse1, problemResponse2));

        mockMvc.perform(get("/problems"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Title 1"))
                .andExpect(jsonPath("$[0].difficulty").value("Difficulty 1"))
                .andExpect(jsonPath("$[0].algorithm").value("Algorithm 1"))
                .andExpect(jsonPath("$[0].solved").value(true))
                .andExpect(jsonPath("$[0].notes").value("Notes 1"))
                .andExpect(jsonPath("$[0].url").value("Url 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("Title 2"))
                .andExpect(jsonPath("$[1].difficulty").value("Difficulty 2"))
                .andExpect(jsonPath("$[1].algorithm").value("Algorithm 2"))
                .andExpect(jsonPath("$[1].solved").value(false))
                .andExpect(jsonPath("$[1].notes").value("Notes 2"))
                .andExpect(jsonPath("$[1].url").value("Url 2"));

        verify(problemService).getAllProblems();
    }

    @Test
    void getProblemsShouldReturn200WhenNoProblemsExist() throws Exception {
        when(problemService.getAllProblems()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/problems"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(problemService).getAllProblems();
    }

    @Test
    void getProblemByIdShouldReturn200WhenProblemExists() throws Exception {
        ProblemResponse problemResponse = new ProblemResponse(
                1L,
                "Title",
                "Difficulty",
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        when(problemService.getProblemById(1L)).thenReturn(problemResponse);

        mockMvc.perform(get("/problems/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.difficulty").value("Difficulty"))
                .andExpect(jsonPath("$.algorithm").value("Algorithm"))
                .andExpect(jsonPath("$.solved").value(true))
                .andExpect(jsonPath("$.notes").value("Notes"))
                .andExpect(jsonPath("$.url").value("Url"));

        verify(problemService).getProblemById(1L);
    }

    @Test
    void getProblemByIdShouldReturn404WhenProblemDoesNotExist() throws Exception {
        when(problemService.getProblemById(99L))
                .thenThrow(new ProblemNotFoundException("Problem with id 99 was not found"));

        mockMvc.perform(get("/problems/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Problem with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/problems/99"));

        verify(problemService).getProblemById(99L);
    }

    @Test
    void createProblemShouldReturn201WhenRequestIsValid() throws Exception {
        CreateProblemRequest request = new CreateProblemRequest();

        request.setTitle("Title");
        request.setDifficulty("Difficulty");
        request.setAlgorithm("Algorithm");
        request.setSolved(true);
        request.setNotes("Notes");
        request.setUrl("Url");

        ProblemResponse problemResponse = new ProblemResponse(
                1L,
                "Title",
                "Difficulty",
                "Algorithm",
                true,
                "Notes",
                "Url"
        );

        when(problemService.createProblem(any(CreateProblemRequest.class))).thenReturn(problemResponse);

        mockMvc.perform(post("/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", "/problems/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.difficulty").value("Difficulty"))
                .andExpect(jsonPath("$.algorithm").value("Algorithm"))
                .andExpect(jsonPath("$.solved").value(true))
                .andExpect(jsonPath("$.notes").value("Notes"))
                .andExpect(jsonPath("$.url").value("Url"));

        ArgumentCaptor<CreateProblemRequest> argumentCaptor = ArgumentCaptor.forClass(CreateProblemRequest.class);

        verify(problemService).createProblem(argumentCaptor.capture());

        CreateProblemRequest capturedRequest = argumentCaptor.getValue();

        assertEquals("Title", capturedRequest.getTitle());
        assertEquals("Difficulty", capturedRequest.getDifficulty());
        assertEquals("Algorithm", capturedRequest.getAlgorithm());
        assertTrue(capturedRequest.isSolved());
        assertEquals("Notes", capturedRequest.getNotes());
        assertEquals("Url", capturedRequest.getUrl());
    }

    @Test
    void createProblemShouldReturn400WhenTitleIsBlank() throws Exception {
        CreateProblemRequest invalidRequest = new CreateProblemRequest();

        invalidRequest.setTitle("");
        invalidRequest.setDifficulty("Difficulty");
        invalidRequest.setAlgorithm("Algorithm");
        invalidRequest.setSolved(true);
        invalidRequest.setNotes("Notes");
        invalidRequest.setUrl("Url");

        mockMvc.perform(post("/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Title cannot be empty"))
                .andExpect(jsonPath("$.path").value("/problems"));

        verify(problemService, never()).createProblem(any(CreateProblemRequest.class));
    }

    @Test
    void updateProblemShouldReturn200WhenRequestIsValidAndProblemExists() throws Exception {
        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("Updated Title");
        request.setDifficulty("Updated Difficulty");
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(false);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");

        ProblemResponse problemResponse = new ProblemResponse(
                1L,
                "Updated Title",
                "Updated Difficulty",
                "Updated Algorithm",
                false,
                "Updated Notes",
                "Updated Url"
        );

        when(problemService.updateProblem(eq(1L), any(UpdateProblemRequest.class))).thenReturn(problemResponse);

        mockMvc.perform(put("/problems/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.difficulty").value("Updated Difficulty"))
                .andExpect(jsonPath("$.algorithm").value("Updated Algorithm"))
                .andExpect(jsonPath("$.solved").value(false))
                .andExpect(jsonPath("$.notes").value("Updated Notes"))
                .andExpect(jsonPath("$.url").value("Updated Url"));

        ArgumentCaptor<UpdateProblemRequest> argumentCaptor = ArgumentCaptor.forClass(UpdateProblemRequest.class);

        verify(problemService).updateProblem(eq(1L), argumentCaptor.capture());

        UpdateProblemRequest capturedRequest = argumentCaptor.getValue();

        assertEquals("Updated Title", capturedRequest.getTitle());
        assertEquals("Updated Difficulty", capturedRequest.getDifficulty());
        assertEquals("Updated Algorithm", capturedRequest.getAlgorithm());
        assertFalse(capturedRequest.isSolved());
        assertEquals("Updated Notes", capturedRequest.getNotes());
        assertEquals("Updated Url", capturedRequest.getUrl());
    }

    @Test
    void updateProblemShouldReturn400WhenTitleIsBlank() throws Exception {
        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("");
        request.setDifficulty("Updated Difficulty");
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(false);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");

        mockMvc.perform(put("/problems/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Title cannot be empty"))
                .andExpect(jsonPath("$.path").value("/problems/1"));

        verify(problemService, never()).updateProblem(eq(1L), any(UpdateProblemRequest.class));
    }

    @Test
    void updateProblemShouldReturn404WhenProblemDoesNotExist() throws Exception {
        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("Updated Title");
        request.setDifficulty("Updated Difficulty");
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(false);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");


        when(problemService.updateProblem(eq(99L), any(UpdateProblemRequest.class)))
                .thenThrow(new ProblemNotFoundException("Problem with id 99 was not found"));

        mockMvc.perform(put("/problems/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Problem with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/problems/99"));

        verify(problemService).updateProblem(eq(99L), any(UpdateProblemRequest.class));
    }

    @Test
    void deleteProblemShouldReturn204WhenProblemExists() throws Exception {

        mockMvc.perform(delete("/problems/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(problemService).deleteProblem(1L);
    }

    @Test
    void deleteProblemShouldReturn404WhenProblemDoesNotExist() throws Exception {

        doThrow(new ProblemNotFoundException("Problem with id 99 was not found"))
                .when(problemService).deleteProblem(99L);

        mockMvc.perform(delete("/problems/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Problem with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/problems/99"));

        verify(problemService).deleteProblem(99L);
    }
}
