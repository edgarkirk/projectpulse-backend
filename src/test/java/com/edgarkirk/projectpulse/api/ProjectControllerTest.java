package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.domain.ProjectStatus;
import com.edgarkirk.projectpulse.service.ProjectService;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void should_return_created_when_valid_request_is_submitted() throws Exception {
        var response = new ProjectResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Atlas Migration",
                "Jane Doe",
                "Active",
                Instant.parse("2026-01-01T10:15:30Z"));
        when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void should_return_bad_request_when_name_is_blank() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateProjectRequest("", "Jane Doe", ProjectStatus.ACTIVE))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name is required"));
    }

    @Test
    void should_return_bad_request_when_status_is_invalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Atlas Migration\",\"ownerName\":\"Jane Doe\",\"status\":\"INVALID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status must be one of Active, At Risk, Blocked, On Hold"));
    }

    @Test
    void should_return_bad_request_when_json_is_malformed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void should_return_conflict_when_duplicate_name_exists() throws Exception {
        when(projectService.createProject(any(CreateProjectRequest.class)))
                .thenThrow(new DuplicateProjectNameException("Atlas Migration"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("project name already exists: Atlas Migration"));
    }

    @Test
    void should_return_not_found_when_project_is_missing() throws Exception {
        var id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(projectService.getProject(id)).thenThrow(new ProjectNotFoundException(id));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/projects/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("project not found for id 11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void should_return_projects_when_requested() throws Exception {
        when(projectService.listProjects()).thenReturn(java.util.List.of(
                new ProjectResponse(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Newest Project", "Jane Doe", "Active", Instant.parse("2026-01-02T10:15:30Z")),
                new ProjectResponse(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Older Project", "Jane Doe", "Blocked", Instant.parse("2026-01-01T10:15:30Z"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Newest Project"))
                .andExpect(jsonPath("$[1].name").value("Older Project"));
    }
}
