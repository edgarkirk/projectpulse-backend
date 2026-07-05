package com.edgarkirk.projectpulse.api;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.service.ProjectService;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void should_return_created_when_valid_request_is_submitted() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");
        var response = new ProjectResponse(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            "Atlas Migration",
            "Jane Doe",
            "Active",
            Instant.parse("2024-01-01T10:15:30Z"));

        given(projectService.create(request)).willReturn(response);

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("123e4567-e89b-12d3-a456-426614174000"))
            .andExpect(jsonPath("$.name").value("Atlas Migration"))
            .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
            .andExpect(jsonPath("$.status").value("Active"))
            .andExpect(jsonPath("$.createdAt").value("2024-01-01T10:15:30Z"));
    }

    @Test
    void should_return_bad_request_when_project_name_is_blank() throws Exception {
        var request = new CreateProjectRequest("", "Jane Doe", "Active");

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("name: Project name is required"));

        then(projectService).shouldHaveNoInteractions();
    }

    @Test
    void should_return_bad_request_when_project_name_exceeds_maximum_length() throws Exception {
        var request = new CreateProjectRequest(repeat('a', 101), "Jane Doe", "Active");

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("name: Project name must not exceed 100 characters"));

        then(projectService).shouldHaveNoInteractions();
    }

    @Test
    void should_return_bad_request_when_owner_name_exceeds_maximum_length() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", repeat('b', 101), "Active");

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("ownerName: Project owner name must not exceed 100 characters"));

        then(projectService).shouldHaveNoInteractions();
    }

    @Test
    void should_return_bad_request_when_project_status_is_invalid() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "In Progress");

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(
                "status: Project status must be one of Active, At Risk, Blocked, or On Hold"));

        then(projectService).shouldHaveNoInteractions();
    }

    @Test
    void should_return_bad_request_when_request_body_is_malformed() throws Exception {
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Request body is malformed JSON"));
    }

    @Test
    void should_return_conflict_when_project_name_is_duplicate() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");
        given(projectService.create(request)).willThrow(
            new DuplicateProjectException("Project name Atlas Migration is already taken"));

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Project name Atlas Migration is already taken"));
    }

    @Test
    void should_return_ok_when_projects_exist() throws Exception {
        var first = new ProjectResponse(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174001"),
            "Newest Project",
            "Owner One",
            "Blocked",
            Instant.parse("2024-01-03T10:15:30Z"));
        var second = new ProjectResponse(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174002"),
            "Older Project",
            "Owner Two",
            "Active",
            Instant.parse("2024-01-02T10:15:30Z"));

        given(projectService.getAll()).willReturn(List.of(first, second));

        mockMvc.perform(get("/api/projects"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Newest Project"))
            .andExpect(jsonPath("$[1].name").value("Older Project"));
    }

    @Test
    void should_return_ok_when_project_exists_by_id() throws Exception {
        var id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        var response = new ProjectResponse(
            id,
            "Atlas Migration",
            "Jane Doe",
            "Active",
            Instant.parse("2024-01-01T10:15:30Z"));

        given(projectService.getById(id)).willReturn(response);

        mockMvc.perform(get("/api/projects/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.name").value("Atlas Migration"));
    }

    @Test
    void should_return_not_found_when_project_does_not_exist() throws Exception {
        var id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        given(projectService.getById(id)).willThrow(
            new ProjectNotFoundException("Project with id 123e4567-e89b-12d3-a456-426614174000 was not found"));

        mockMvc.perform(get("/api/projects/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(
                "Project with id 123e4567-e89b-12d3-a456-426614174000 was not found"));
    }

    @Test
    void should_return_bad_request_when_project_id_is_not_a_uuid() throws Exception {
        mockMvc.perform(get("/api/projects/not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid value for path variable 'id'"));
    }

    @Test
    void should_return_ok_when_dashboard_summary_is_requested() throws Exception {
        given(projectService.getDashboardSummary())
            .willReturn(new DashboardSummary(6L, 3L, 2L, 1L, 0L));

        mockMvc.perform(get("/api/dashboard/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalProjects").value(6))
            .andExpect(jsonPath("$.active").value(3))
            .andExpect(jsonPath("$.atRisk").value(2))
            .andExpect(jsonPath("$.blocked").value(1))
            .andExpect(jsonPath("$.onHold").value(0));
    }

    private static String repeat(char character, int count) {
        return String.valueOf(character).repeat(count);
    }
}
