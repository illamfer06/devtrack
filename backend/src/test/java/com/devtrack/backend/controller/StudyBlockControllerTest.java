package com.devtrack.backend.controller;

import com.devtrack.backend.dto.*;
import com.devtrack.backend.exception.StudyBlockNotFoundException;
import com.devtrack.backend.service.StudyBlockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudyBlockController.class)
class StudyBlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudyBlockService studyBlockService;

    @Test
    void createStudyBlockShouldReturn201WhenRequestIsValid() throws Exception {
        CreateStudyBlockRequest request = new CreateStudyBlockRequest();

        request.setTitle("Title");
        request.setActive(false);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 22, 18, 30);

        StudyBlockResponse response = new StudyBlockResponse(
                1L,
                "Title",
                false,
                createdAt,
                updatedAt
        );

        when(studyBlockService.createStudyBlock(any(CreateStudyBlockRequest.class))).thenReturn(response);

        mockMvc.perform(post("/study-blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-08-22T18:30:00"));

        ArgumentCaptor<CreateStudyBlockRequest> argumentCaptor = ArgumentCaptor.forClass(CreateStudyBlockRequest.class);

        verify(studyBlockService).createStudyBlock(argumentCaptor.capture());

        CreateStudyBlockRequest capturedRequest = argumentCaptor.getValue();

        assertEquals("Title", capturedRequest.getTitle());
        assertFalse(capturedRequest.isActive());
    }

    @Test
    void createStudyBlockShouldReturn400WhenTitleIsBlank() throws Exception {
        CreateStudyBlockRequest invalidRequest = new CreateStudyBlockRequest();

        invalidRequest.setTitle("");
        invalidRequest.setActive(false);

        mockMvc.perform(post("/study-blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Title cannot be empty"))
                .andExpect(jsonPath("$.path").value("/study-blocks"));

        verify(studyBlockService, never()).createStudyBlock(any(CreateStudyBlockRequest.class));
    }

    @Test
    void getStudyBlocksShouldReturn200WhenStudyBlocksExist() throws Exception {
        LocalDateTime createdAt1 = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt1 = LocalDateTime.of(2026, 8, 22, 18, 30);

        LocalDateTime createdAt2 = LocalDateTime.of(2026, 8, 21, 17, 0);
        LocalDateTime updatedAt2 = LocalDateTime.of(2026, 8, 22, 12, 15);

        StudyBlockResponse studyBlockResponse1 = new StudyBlockResponse(
                1L,
                "Title 1",
                false,
                createdAt1,
                updatedAt1
        );

        StudyBlockResponse studyBlockResponse2 = new StudyBlockResponse(
                2L,
                "Title 2",
                true,
                createdAt2,
                updatedAt2
        );


        PageResponse<StudyBlockResponse> page = new PageResponse<>(
                List.of(studyBlockResponse1, studyBlockResponse2),
                0,
                2,
                4,
                2
        );

        when(studyBlockService.getStudyBlocks(isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/study-blocks?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Title 1"))
                .andExpect(jsonPath("$.content[0].active").value(false))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.content[0].updatedAt").value("2026-08-22T18:30:00"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("Title 2"))
                .andExpect(jsonPath("$.content[1].active").value(true))
                .andExpect(jsonPath("$.content[1].createdAt").value("2026-08-21T17:00:00"))
                .andExpect(jsonPath("$.content[1].updatedAt").value("2026-08-22T12:15:00"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(studyBlockService).getStudyBlocks(isNull(), any(Pageable.class));
    }

    @Test
    void getStudyBlocksShouldReturn200WhenFilteringByActiveAndSortingByIdAscending() throws Exception {
        LocalDateTime createdAt1 = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt1 = LocalDateTime.of(2026, 8, 22, 18, 30);

        LocalDateTime createdAt2 = LocalDateTime.of(2026, 8, 21, 17, 0);
        LocalDateTime updatedAt2 = LocalDateTime.of(2026, 8, 22, 12, 15);

        StudyBlockResponse studyBlockResponse1 = new StudyBlockResponse(
                1L,
                "Title 1",
                false,
                createdAt1,
                updatedAt1
        );

        StudyBlockResponse studyBlockResponse2 = new StudyBlockResponse(
                2L,
                "Title 2",
                false,
                createdAt2,
                updatedAt2
        );

        PageResponse<StudyBlockResponse> page = new PageResponse<>(
                List.of(studyBlockResponse1, studyBlockResponse2),
                0,
                2,
                2,
                1
        );

        when(studyBlockService.getStudyBlocks(eq(false), any(Pageable.class))).thenReturn(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/study-blocks?active=false&page=0&size=2&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Title 1"))
                .andExpect(jsonPath("$.content[0].active").value(false))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.content[0].updatedAt").value("2026-08-22T18:30:00"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("Title 2"))
                .andExpect(jsonPath("$.content[1].active").value(false))
                .andExpect(jsonPath("$.content[1].createdAt").value("2026-08-21T17:00:00"))
                .andExpect(jsonPath("$.content[1].updatedAt").value("2026-08-22T12:15:00"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(studyBlockService).getStudyBlocks(eq(false), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(2, capturedPageable.getPageSize());

        Sort.Order order = capturedPageable.getSort().getOrderFor("id");

        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void getStudyBlocksShouldReturn200AndEmptyListWhenNoStudyBlocksMatchActive() throws Exception {

        PageResponse<StudyBlockResponse> page = new PageResponse<>(
                Collections.emptyList(),
                0,
                2,
                0,
                0
        );

        when(studyBlockService.getStudyBlocks(eq(false), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/study-blocks?page=0&size=2&active=false"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(studyBlockService).getStudyBlocks(eq(false), any(Pageable.class));
    }

    @Test
    void getStudyBlocksShouldReturn400WhenActiveIsInvalid() throws Exception {
        mockMvc.perform(get("/study-blocks?active=invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Active must be true or false"))
                .andExpect(jsonPath("$.path").value("/study-blocks"));

        verify(studyBlockService, never()).getStudyBlocks(any(), any(Pageable.class));
    }

    @Test
    void getStudyBlockByIdShouldReturn200WhenStudyBlockExists() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 22, 18, 30);

        StudyBlockResponse studyBlockResponse = new StudyBlockResponse(
                1L,
                "Title",
                false,
                createdAt,
                updatedAt
        );

        when(studyBlockService.getStudyBlockById(1L)).thenReturn(studyBlockResponse);

        mockMvc.perform(get("/study-blocks/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-08-22T18:30:00"));

        verify(studyBlockService).getStudyBlockById(1L);
    }

    @Test
    void getStudyBlockByIdShouldReturn404WhenStudyBlockDoesNotExist() throws Exception {
        when(studyBlockService.getStudyBlockById(99L)).thenThrow(new StudyBlockNotFoundException("Study block with id 99 was not found"));

        mockMvc.perform(get("/study-blocks/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Study block with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/study-blocks/99"));

        verify(studyBlockService).getStudyBlockById(99L);
    }

    @Test
    void updateStudyBlockShouldReturn200WhenRequestIsValidAndStudyBlockExists() throws Exception {
        UpdateStudyBlockRequest request = new UpdateStudyBlockRequest();

        request.setTitle("Updated Title");
        request.setActive(true);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 18, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 22, 18, 30);

        StudyBlockResponse response = new StudyBlockResponse(
                1L,
                "Updated Title",
                true,
                createdAt,
                updatedAt
        );

        when(studyBlockService.updateStudyBlock(eq(1L), any(UpdateStudyBlockRequest.class))).thenReturn(response);

        mockMvc.perform(put("/study-blocks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").value("2026-08-22T18:00:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-08-22T18:30:00"));

        ArgumentCaptor<UpdateStudyBlockRequest> argumentCaptor = ArgumentCaptor.forClass(UpdateStudyBlockRequest.class);

        verify(studyBlockService).updateStudyBlock(eq(1L), argumentCaptor.capture());

        UpdateStudyBlockRequest capturedRequest = argumentCaptor.getValue();

        assertEquals("Updated Title", capturedRequest.getTitle());
        assertTrue(capturedRequest.isActive());
    }

    @Test
    void updateStudyBlockShouldReturn400WhenTitleIsBlank() throws Exception {
        UpdateStudyBlockRequest invalidRequest = new UpdateStudyBlockRequest();

        invalidRequest.setTitle("");
        invalidRequest.setActive(true);

        mockMvc.perform(put("/study-blocks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Title cannot be empty"))
                .andExpect(jsonPath("$.path").value("/study-blocks/1"));

        verify(studyBlockService, never()).updateStudyBlock(eq(1L), any(UpdateStudyBlockRequest.class));
    }

    @Test
    void updateStudyBlockShouldReturn404WhenStudyBlockDoesNotExist() throws Exception {
        UpdateStudyBlockRequest request = new UpdateStudyBlockRequest();

        request.setTitle("Updated Title");
        request.setActive(true);

        when(studyBlockService.updateStudyBlock(eq(99L), any(UpdateStudyBlockRequest.class)))
                .thenThrow(new StudyBlockNotFoundException("Study block with id 99 was not found"));

        mockMvc.perform(put("/study-blocks/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Study block with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/study-blocks/99"));

        verify(studyBlockService).updateStudyBlock(eq(99L), any(UpdateStudyBlockRequest.class));
    }

    @Test
    void deleteStudyBlockShouldReturn204WhenStudyBlockExists() throws Exception {

        mockMvc.perform(delete("/study-blocks/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(studyBlockService).deleteStudyBlock(1L);
    }

    @Test
    void deleteStudyBlockShouldReturn404WhenStudyBlockDoesNotExist() throws Exception {

        doThrow(new StudyBlockNotFoundException("Study block with id 99 was not found"))
                .when(studyBlockService).deleteStudyBlock(99L);

        mockMvc.perform(delete("/study-blocks/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Study block with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/study-blocks/99"));

        verify(studyBlockService).deleteStudyBlock(99L);
    }
}
