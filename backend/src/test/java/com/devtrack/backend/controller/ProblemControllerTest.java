package com.devtrack.backend.controller;

import com.devtrack.backend.dto.CreateProblemRequest;
import com.devtrack.backend.dto.PageResponse;
import com.devtrack.backend.dto.ProblemResponse;
import com.devtrack.backend.dto.UpdateProblemRequest;
import com.devtrack.backend.exception.ProblemNotFoundException;
import com.devtrack.backend.exception.StudyBlockNotFoundException;
import com.devtrack.backend.model.Difficulty;
import com.devtrack.backend.service.ProblemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void createProblemShouldReturn201WhenRequestIsValid() throws Exception {
        CreateProblemRequest request = new CreateProblemRequest();

        request.setTitle("Title");
        request.setDifficulty(Difficulty.EASY);
        request.setAlgorithm("Algorithm");
        request.setSolved(true);
        request.setNotes("Notes");
        request.setUrl("Url");

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 22, 18, 30);

        ProblemResponse problemResponse = new ProblemResponse(
                1L,
                "Title",
                Difficulty.EASY,
                "Algorithm",
                true,
                "Notes",
                "Url",
                createdAt,
                updatedAt
        );

        when(problemService.createProblem(eq(1L), any(CreateProblemRequest.class))).thenReturn(problemResponse);

        mockMvc.perform(post("/study-blocks/{studyBlockId}/problems", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", "/study-blocks/1/problems/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.algorithm").value("Algorithm"))
                .andExpect(jsonPath("$.solved").value(true))
                .andExpect(jsonPath("$.notes").value("Notes"))
                .andExpect(jsonPath("$.url").value("Url"))
                .andExpect(jsonPath("$.createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-08-22T18:30:00"));

        ArgumentCaptor<CreateProblemRequest> argumentCaptor = ArgumentCaptor.forClass(CreateProblemRequest.class);

        verify(problemService).createProblem(eq(1L), argumentCaptor.capture());

        CreateProblemRequest capturedRequest = argumentCaptor.getValue();

        assertEquals("Title", capturedRequest.getTitle());
        assertEquals(Difficulty.EASY, capturedRequest.getDifficulty());
        assertEquals("Algorithm", capturedRequest.getAlgorithm());
        assertTrue(capturedRequest.isSolved());
        assertEquals("Notes", capturedRequest.getNotes());
        assertEquals("Url", capturedRequest.getUrl());
    }

    @Test
    void createProblemShouldReturn400WhenTitleIsBlank() throws Exception {
        CreateProblemRequest invalidRequest = new CreateProblemRequest();

        invalidRequest.setTitle("");
        invalidRequest.setDifficulty(Difficulty.EASY);
        invalidRequest.setAlgorithm("Algorithm");
        invalidRequest.setSolved(true);
        invalidRequest.setNotes("Notes");
        invalidRequest.setUrl("Url");

        mockMvc.perform(post("/study-blocks/{studyBlockId}/problems", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Title cannot be empty"))
                .andExpect(jsonPath("$.path").value("/study-blocks/1/problems"));

        verify(problemService, never()).createProblem(eq(1L), any(CreateProblemRequest.class));
    }

    @Test
    void createProblemShouldReturn400WhenDifficultyIsInvalid() throws Exception {
        String json = """
                {
                    "title": "Title",
                    "difficulty": "IMPOSSIBLE",
                    "algorithm": "Algorithm",
                    "solved": true,
                    "notes": "Notes",
                    "url": "URL"
                }
                """;

        mockMvc.perform(post("/study-blocks/{studyBlockId}/problems", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request body"))
                .andExpect(jsonPath("$.path").value("/study-blocks/1/problems"));

        verify(problemService, never()).createProblem(eq(1L), any(CreateProblemRequest.class));
    }

    @Test
    void createProblemShouldReturn404WhenStudyBlockDoesNotExist() throws Exception {
        CreateProblemRequest request = new CreateProblemRequest();

        request.setTitle("Title");
        request.setDifficulty(Difficulty.EASY);
        request.setAlgorithm("Algorithm");
        request.setSolved(true);
        request.setNotes("Notes");
        request.setUrl("Url");

        when(problemService.createProblem(eq(99L), any(CreateProblemRequest.class)))
                .thenThrow(new StudyBlockNotFoundException("Study block with id 99 was not found"));

        mockMvc.perform(post("/study-blocks/{studyBlockId}/problems", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Study block with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/study-blocks/99/problems"));

        verify(problemService).createProblem(eq(99L), any(CreateProblemRequest.class));
    }

    @Test
    void getProblemsShouldReturn200WhenProblemsExist() throws Exception {
        LocalDateTime createdAt1 = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt1 = LocalDateTime.of(2026, 8, 22, 18, 30);

        LocalDateTime createdAt2 = LocalDateTime.of(2026, 8, 21, 17, 0);
        LocalDateTime updatedAt2 = LocalDateTime.of(2026, 8, 22, 12, 15);

        ProblemResponse problemResponse1 = new ProblemResponse(
                1L,
                "Title 1",
                Difficulty.EASY,
                "Algorithm 1",
                true,
                "Notes 1",
                "Url 1",
                createdAt1,
                updatedAt1
        );

        ProblemResponse problemResponse2 = new ProblemResponse(
                2L,
                "Title 2",
                Difficulty.HARD,
                "Algorithm 2",
                false,
                "Notes 2",
                "Url 2",
                createdAt2,
                updatedAt2
        );

        PageResponse<ProblemResponse> response = new PageResponse<>(
                List.of(problemResponse1, problemResponse2),
                0,
                2,
                5,
                3
        );

        when(problemService.getProblems(eq(1L), isNull(), isNull(), any(Pageable.class))).thenReturn(response);

        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Title 1"))
                .andExpect(jsonPath("$.content[0].difficulty").value("EASY"))
                .andExpect(jsonPath("$.content[0].algorithm").value("Algorithm 1"))
                .andExpect(jsonPath("$.content[0].solved").value(true))
                .andExpect(jsonPath("$.content[0].notes").value("Notes 1"))
                .andExpect(jsonPath("$.content[0].url").value("Url 1"))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.content[0].updatedAt").value("2026-08-22T18:30:00"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].title").value("Title 2"))
                .andExpect(jsonPath("$.content[1].difficulty").value("HARD"))
                .andExpect(jsonPath("$.content[1].algorithm").value("Algorithm 2"))
                .andExpect(jsonPath("$.content[1].solved").value(false))
                .andExpect(jsonPath("$.content[1].notes").value("Notes 2"))
                .andExpect(jsonPath("$.content[1].url").value("Url 2"))
                .andExpect(jsonPath("$.content[1].createdAt").value("2026-08-21T17:00:00"))
                .andExpect(jsonPath("$.content[1].updatedAt").value("2026-08-22T12:15:00"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(problemService).getProblems(eq(1L), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getProblemsShouldReturn200WhenNoProblemsExist() throws Exception {
        PageResponse<ProblemResponse> response = new PageResponse<>(
                Collections.emptyList(),
                0,
                2,
                0,
                0
        );

        when(problemService.getProblems(eq(1L), isNull(), isNull(), any(Pageable.class))).thenReturn(response);

        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems?page=0&size=2", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(problemService).getProblems(eq(1L), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getProblemsShouldReturn404WhenStudyBlockDoesNotExist() throws Exception {

        when(problemService.getProblems(eq(99L), isNull(), isNull(), any(Pageable.class))).
                thenThrow(new StudyBlockNotFoundException("Study block with id 99 was not found"));

        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Study block with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/study-blocks/99/problems"));

        verify(problemService).getProblems(eq(99L), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getProblemsShouldReturn200WhenFilteringByDifficulty() throws Exception {
        LocalDateTime createdAt1 = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt1 = LocalDateTime.of(2026, 8, 22, 18, 30);

        LocalDateTime createdAt2 = LocalDateTime.of(2026, 8, 21, 17, 0);
        LocalDateTime updatedAt2 = LocalDateTime.of(2026, 8, 22, 12, 15);

        ProblemResponse problemResponse1 = new ProblemResponse(
                1L,
                "Title 1",
                Difficulty.EASY,
                "Algorithm 1",
                true,
                "Notes 1",
                "Url 1",
                createdAt1,
                updatedAt1
        );

        ProblemResponse problemResponse2 = new ProblemResponse(
                2L,
                "Title 2",
                Difficulty.EASY,
                "Algorithm 2",
                false,
                "Notes 2",
                "Url 2",
                createdAt2,
                updatedAt2
        );

        PageResponse<ProblemResponse> response = new PageResponse<>(
                List.of(problemResponse1, problemResponse2),
                0,
                2,
                5,
                3
        );

        when(problemService.getProblems(eq(1L), eq(Difficulty.EASY), isNull(), any(Pageable.class))).thenReturn(response);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems?difficulty=EASY&page=0&size=2", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Title 1"))
                .andExpect(jsonPath("$.content[0].difficulty").value("EASY"))
                .andExpect(jsonPath("$.content[0].algorithm").value("Algorithm 1"))
                .andExpect(jsonPath("$.content[0].solved").value(true))
                .andExpect(jsonPath("$.content[0].notes").value("Notes 1"))
                .andExpect(jsonPath("$.content[0].url").value("Url 1"))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.content[0].updatedAt").value("2026-08-22T18:30:00"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].title").value("Title 2"))
                .andExpect(jsonPath("$.content[1].difficulty").value("EASY"))
                .andExpect(jsonPath("$.content[1].algorithm").value("Algorithm 2"))
                .andExpect(jsonPath("$.content[1].solved").value(false))
                .andExpect(jsonPath("$.content[1].notes").value("Notes 2"))
                .andExpect(jsonPath("$.content[1].url").value("Url 2"))
                .andExpect(jsonPath("$.content[1].createdAt").value("2026-08-21T17:00:00"))
                .andExpect(jsonPath("$.content[1].updatedAt").value("2026-08-22T12:15:00"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(problemService).getProblems(eq(1L), eq(Difficulty.EASY), isNull(), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(2, capturedPageable.getPageSize());
    }

    @Test
    void getProblemsShouldReturn200AndEmptyListWhenNoProblemMatchDifficulty() throws Exception {
        PageResponse<ProblemResponse> response = new PageResponse<>(
                Collections.emptyList(),
                0,
                2,
                0,
                0
        );
        when(problemService.getProblems(eq(1L), eq(Difficulty.HARD), isNull(), any(Pageable.class))).thenReturn(response);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems?difficulty=HARD&page=0&size=2", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(problemService).getProblems(eq(1L), eq(Difficulty.HARD), isNull(), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(2, capturedPageable.getPageSize());
    }

    @Test
    void getProblemsShouldReturn400WhenDifficultyIsInvalid() throws Exception {
        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems?difficulty=IMPOSSIBLE", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Difficulty must be one of: EASY, MEDIUM, HARD"))
                .andExpect(jsonPath("$.path").value("/study-blocks/1/problems"));

        verify(problemService, never()).getProblems(any(), any(), any(), any());
    }

    @Test
    void getProblemsShouldReturn200WhenFilteringBySolved() throws Exception {
        LocalDateTime createdAt1 = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt1 = LocalDateTime.of(2026, 8, 22, 18, 30);

        LocalDateTime createdAt2 = LocalDateTime.of(2026, 8, 21, 17, 0);
        LocalDateTime updatedAt2 = LocalDateTime.of(2026, 8, 22, 12, 15);

        ProblemResponse problemResponse1 = new ProblemResponse(
                1L,
                "Title 1",
                Difficulty.EASY,
                "Algorithm 1",
                true,
                "Notes 1",
                "Url 1",
                createdAt1,
                updatedAt1
        );

        ProblemResponse problemResponse2 = new ProblemResponse(
                2L,
                "Title 2",
                Difficulty.EASY,
                "Algorithm 2",
                true,
                "Notes 2",
                "Url 2",
                createdAt2,
                updatedAt2
        );

        PageResponse<ProblemResponse> response = new PageResponse<>(
                List.of(problemResponse1, problemResponse2),
                0,
                2,
                5,
                3
        );

        when(problemService.getProblems(eq(1L), isNull(),eq(true), any(Pageable.class))).thenReturn(response);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems?solved=true&page=0&size=2", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Title 1"))
                .andExpect(jsonPath("$.content[0].difficulty").value("EASY"))
                .andExpect(jsonPath("$.content[0].algorithm").value("Algorithm 1"))
                .andExpect(jsonPath("$.content[0].solved").value(true))
                .andExpect(jsonPath("$.content[0].notes").value("Notes 1"))
                .andExpect(jsonPath("$.content[0].url").value("Url 1"))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.content[0].updatedAt").value("2026-08-22T18:30:00"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].title").value("Title 2"))
                .andExpect(jsonPath("$.content[1].difficulty").value("EASY"))
                .andExpect(jsonPath("$.content[1].algorithm").value("Algorithm 2"))
                .andExpect(jsonPath("$.content[1].solved").value(true))
                .andExpect(jsonPath("$.content[1].notes").value("Notes 2"))
                .andExpect(jsonPath("$.content[1].url").value("Url 2"))
                .andExpect(jsonPath("$.content[1].createdAt").value("2026-08-21T17:00:00"))
                .andExpect(jsonPath("$.content[1].updatedAt").value("2026-08-22T12:15:00"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(problemService).getProblems(eq(1L), isNull(), eq(true), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(2, capturedPageable.getPageSize());
    }

    @Test
    void getProblemsShouldReturn200AndEmptyListWhenNoProblemsMatchSolved() throws Exception {
        PageResponse<ProblemResponse> response = new PageResponse<>(
                Collections.emptyList(),
                0,
                2,
                0,
                0
        );
        when(problemService.getProblems(eq(1L), isNull(),eq(false), any(Pageable.class))).thenReturn(response);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems?solved=false&page=0&size=2", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(problemService).getProblems(eq(1L), isNull(), eq(false), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(2, capturedPageable.getPageSize());
    }

    @Test
    void getProblemsShouldReturn400WhenSolvedIsInvalid() throws Exception {
        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems?solved=invalid", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Solved must be true or false"))
                .andExpect(jsonPath("$.path").value("/study-blocks/1/problems"));

        verify(problemService, never()).getProblems(any(), any(), any(), any(Pageable.class));
    }

    @Test
    void getProblemsShouldReturn200WhenFilteringByDifficultyAndSolved() throws Exception {
        LocalDateTime createdAt1 = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt1 = LocalDateTime.of(2026, 8, 22, 18, 30);

        LocalDateTime createdAt2 = LocalDateTime.of(2026, 8, 21, 17, 0);
        LocalDateTime updatedAt2 = LocalDateTime.of(2026, 8, 22, 12, 15);

        ProblemResponse problemResponse1 = new ProblemResponse(
                1L,
                "Title 1",
                Difficulty.EASY,
                "Algorithm 1",
                true,
                "Notes 1",
                "Url 1",
                createdAt1,
                updatedAt1
        );

        ProblemResponse problemResponse2 = new ProblemResponse(
                2L,
                "Title 2",
                Difficulty.EASY,
                "Algorithm 2",
                true,
                "Notes 2",
                "Url 2",
                createdAt2,
                updatedAt2
        );

        PageResponse<ProblemResponse> response = new PageResponse<>(
                List.of(problemResponse1, problemResponse2),
                2,
                5,
                15,
                3
        );

        when(problemService.getProblems(eq(1L), eq(Difficulty.EASY), eq(true), any(Pageable.class))).thenReturn(response);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems?difficulty=EASY&solved=true&page=2&size=5", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Title 1"))
                .andExpect(jsonPath("$.content[0].difficulty").value("EASY"))
                .andExpect(jsonPath("$.content[0].algorithm").value("Algorithm 1"))
                .andExpect(jsonPath("$.content[0].solved").value(true))
                .andExpect(jsonPath("$.content[0].notes").value("Notes 1"))
                .andExpect(jsonPath("$.content[0].url").value("Url 1"))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.content[0].updatedAt").value("2026-08-22T18:30:00"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].title").value("Title 2"))
                .andExpect(jsonPath("$.content[1].difficulty").value("EASY"))
                .andExpect(jsonPath("$.content[1].algorithm").value("Algorithm 2"))
                .andExpect(jsonPath("$.content[1].solved").value(true))
                .andExpect(jsonPath("$.content[1].notes").value("Notes 2"))
                .andExpect(jsonPath("$.content[1].url").value("Url 2"))
                .andExpect(jsonPath("$.content[1].createdAt").value("2026-08-21T17:00:00"))
                .andExpect(jsonPath("$.content[1].updatedAt").value("2026-08-22T12:15:00"))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(problemService).getProblems(eq(1L), eq(Difficulty.EASY), eq(true), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(2, capturedPageable.getPageNumber());
        assertEquals(5, capturedPageable.getPageSize());
    }

    @Test
    void getProblemsShouldReturn200AndEmptyListWhenNoProblemsMatchDifficultyAndSolved() throws Exception {
        PageResponse<ProblemResponse> response = new PageResponse<>(
                Collections.emptyList(),
                0,
                2,
                0,
                0
        );

        when(problemService.getProblems(eq(1L), eq(Difficulty.HARD), eq(false), any(Pageable.class))).thenReturn(response);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems?difficulty=HARD&solved=false&page=0&size=2", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(problemService).getProblems(eq(1L), eq(Difficulty.HARD), eq(false), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(2, capturedPageable.getPageSize());
    }

    @Test
    void getProblemsShouldReturn200WhenSortingByIdDescending() throws Exception {
        LocalDateTime createdAt1 = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt1 = LocalDateTime.of(2026, 8, 22, 18, 30);

        LocalDateTime createdAt2 = LocalDateTime.of(2026, 8, 21, 17, 0);
        LocalDateTime updatedAt2 = LocalDateTime.of(2026, 8, 22, 12, 15);

        ProblemResponse problemResponse1 = new ProblemResponse(
                1L,
                "Title1",
                Difficulty.EASY,
                "Algorithm1",
                false,
                "Notes1",
                "Url1",
                createdAt1,
                updatedAt1
        );

        ProblemResponse problemResponse2 = new ProblemResponse(
                2L,
                "Title2",
                Difficulty.MEDIUM,
                "Algorithm2",
                false,
                "Notes2",
                "Url2",
                createdAt2,
                updatedAt2
        );

        PageResponse<ProblemResponse> response = new PageResponse<>(
                List.of(problemResponse2, problemResponse1),
                0,
                2,
                4,
                2
        );

        when(problemService.getProblems(eq(1L), isNull(), isNull(), any(Pageable.class))).thenReturn(response);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems?page=0&size=2&sort=id,desc", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(2L))
                .andExpect(jsonPath("$.content[1].id").value(1L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(problemService).getProblems(eq(1L), isNull(), isNull(), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(2, capturedPageable.getPageSize());

        Sort.Order order = capturedPageable.getSort().getOrderFor("id");

        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getProblemsByIdShouldReturn200WhenProblemExists() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 22, 18, 30);

        ProblemResponse problemResponse = new ProblemResponse(
                1L,
                "Title",
                Difficulty.EASY,
                "Algorithm",
                true,
                "Notes",
                "Url",
                createdAt,
                updatedAt
        );

        when(problemService.getProblemById(1L, 5L)).thenReturn(problemResponse);

        mockMvc.perform(get("/study-blocks/{studyBlocksId}/problems/{problemId}", 5L, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.algorithm").value("Algorithm"))
                .andExpect(jsonPath("$.solved").value(true))
                .andExpect(jsonPath("$.notes").value("Notes"))
                .andExpect(jsonPath("$.url").value("Url"))
                .andExpect(jsonPath("$.createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-08-22T18:30:00"));

        verify(problemService).getProblemById(1L, 5L);
    }

    @Test
    void getProblemsByIdShouldReturn404WhenStudyBlockDoesNotExist() throws Exception {
        when(problemService.getProblemById(1L, 99L))
                .thenThrow(new StudyBlockNotFoundException("Study block with id 99 was not found"));

        mockMvc.perform(get("/study-blocks/{studyBlocksId}/problems/{problemId}", 99L, 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Study block with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/study-blocks/99/problems/1"));

        verify(problemService).getProblemById(1L, 99L);
    }

    @Test
    void getProblemsByIdShouldReturn404WhenProblemDoesNotExist() throws Exception {
        when(problemService.getProblemById(99L, 1L))
                .thenThrow(new ProblemNotFoundException("Problem with id 99 was not found in study block 1"));

        mockMvc.perform(get("/study-blocks/{studyBlockId}/problems/{id}", 1L, 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Problem with id 99 was not found in study block 1"))
                .andExpect(jsonPath("$.path").value("/study-blocks/1/problems/99"));

        verify(problemService).getProblemById(99L, 1L);
    }

    @Test
    void updateProblemShouldReturn200WhenRequestIsValidAndProblemExists() throws Exception {
        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("Updated Title");
        request.setDifficulty(Difficulty.EASY);
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(false);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 22, 18, 30);

        ProblemResponse problemResponse = new ProblemResponse(
                1L,
                "Updated Title",
                Difficulty.EASY,
                "Updated Algorithm",
                false,
                "Updated Notes",
                "Updated Url",
                createdAt,
                updatedAt
        );

        when(problemService.updateProblem(eq(1L), eq(5L), any(UpdateProblemRequest.class))).thenReturn(problemResponse);

        mockMvc.perform(put("/study-blocks/{studyBlockId}/problems/{problemId}",5L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.algorithm").value("Updated Algorithm"))
                .andExpect(jsonPath("$.solved").value(false))
                .andExpect(jsonPath("$.notes").value("Updated Notes"))
                .andExpect(jsonPath("$.url").value("Updated Url"))
                .andExpect(jsonPath("$.createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-08-22T18:30:00"));

        ArgumentCaptor<UpdateProblemRequest> argumentCaptor = ArgumentCaptor.forClass(UpdateProblemRequest.class);

        verify(problemService).updateProblem(eq(1L), eq(5L), argumentCaptor.capture());

        UpdateProblemRequest capturedRequest = argumentCaptor.getValue();

        assertEquals("Updated Title", capturedRequest.getTitle());
        assertEquals(Difficulty.EASY, capturedRequest.getDifficulty());
        assertEquals("Updated Algorithm", capturedRequest.getAlgorithm());
        assertFalse(capturedRequest.isSolved());
        assertEquals("Updated Notes", capturedRequest.getNotes());
        assertEquals("Updated Url", capturedRequest.getUrl());
    }

    @Test
    void updateProblemShouldReturn400WhenTitleIsBlank() throws Exception {
        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("");
        request.setDifficulty(Difficulty.EASY);
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(false);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");

        mockMvc.perform(put("/study-blocks/{studyBlockId}/problems/{problemId}",5L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Title cannot be empty"))
                .andExpect(jsonPath("$.path").value("/study-blocks/5/problems/1"));

        verify(problemService, never()).updateProblem(anyLong(), anyLong(), any(UpdateProblemRequest.class));
    }

    @Test
    void updateProblemShouldReturn400WhenDifficultyIsInvalid() throws Exception {
        String json = """
                {
                    "title": "Updated Title",
                    "difficulty": "IMPOSSIBLE",
                    "algorithm": "Updated Algorithm",
                    "solved": true,
                    "notes": "Updated Notes",
                    "url": "Updated Url"
                }
                """;

        mockMvc.perform(put("/study-blocks/{studyBlockId}/problems/{problemId}", 5L,1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request body"))
                .andExpect(jsonPath("$.path").value("/study-blocks/5/problems/1"));

        verify(problemService, never()).updateProblem(anyLong(), anyLong(), any());
    }

    @Test
    void updateProblemShouldReturn404WhenStudyBlockDoesNotExist() throws Exception {
        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("Updated Title");
        request.setDifficulty(Difficulty.EASY);
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(false);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");


        when(problemService.updateProblem(eq(1L), eq(99L), any(UpdateProblemRequest.class)))
                .thenThrow(new StudyBlockNotFoundException("Study block with id 99 was not found"));

        mockMvc.perform(put("/study-blocks/{studyBlockId}/problems/{problemId}", 99L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Study block with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/study-blocks/99/problems/1"));

        verify(problemService).updateProblem(eq(1L), eq(99L), any(UpdateProblemRequest.class));
    }

    @Test
    void updateProblemShouldReturn404WhenProblemDoesNotExist() throws Exception {
        UpdateProblemRequest request = new UpdateProblemRequest();

        request.setTitle("Updated Title");
        request.setDifficulty(Difficulty.EASY);
        request.setAlgorithm("Updated Algorithm");
        request.setSolved(false);
        request.setNotes("Updated Notes");
        request.setUrl("Updated Url");


        when(problemService.updateProblem(eq(99L), eq(1L), any(UpdateProblemRequest.class)))
                .thenThrow(new ProblemNotFoundException("Problem with id 99 was not found in study block 1"));

        mockMvc.perform(put("/study-blocks/{studyBlockId}/problems/{problemId}", 1L, 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Problem with id 99 was not found in study block 1"))
                .andExpect(jsonPath("$.path").value("/study-blocks/1/problems/99"));

        verify(problemService).updateProblem(eq(99L), eq(1L), any(UpdateProblemRequest.class));
    }

    @Test
    void deleteProblemShouldReturn204WhenProblemExists() throws Exception {

        mockMvc.perform(delete("/study-blocks/{studyBlockId}/problems/{problemId}", 5L, 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(problemService).deleteProblem(1L, 5L);
    }

    @Test
    void deleteProblemShouldReturn404WhenStudyBlockDoesNotExist() throws Exception {

        doThrow(new StudyBlockNotFoundException("Study block with id 99 was not found"))
                .when(problemService).deleteProblem(1L, 99L);

        mockMvc.perform(delete("/study-blocks/{studyBlockId}/problems/{problemId}", 99L, 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Study block with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/study-blocks/99/problems/1"));

        verify(problemService).deleteProblem(1L, 99L);
    }

    @Test
    void deleteProblemShouldReturn404WhenProblemDoesNotExist() throws Exception {

        doThrow(new ProblemNotFoundException("Problem with id 99 was not found in study block 1"))
                .when(problemService).deleteProblem(99L, 1L);

        mockMvc.perform(delete("/study-blocks/{studyBlockId}/problems/{problemId}", 1L, 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Problem with id 99 was not found in study block 1"))
                .andExpect(jsonPath("$.path").value("/study-blocks/1/problems/99"));

        verify(problemService).deleteProblem(99L, 1L);
    }
}
