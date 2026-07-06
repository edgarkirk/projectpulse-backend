package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.service.ProjectService;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import com.edgarkirk.projectpulse.domain.ProjectStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void should_return_created_when_valid_request() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        var response = new ProjectResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Atlas Migration",
                "Jane Doe",
                "Active",
                OffsetDateTime.parse("2026-01-01T10:15:30Z"));

        when(projectService.create(any(CreateProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T10:15:30Z"));
    }

    @Test
    void should_return_ok_when_project_exists() throws Exception {
        var id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        var response = new ProjectResponse(
                id,
                "Atlas Migration",
                "Jane Doe",
                "Active",
                OffsetDateTime.parse("2026-01-01T10:15:30Z"));

        when(projectService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T10:15:30Z"));
    }

    @Test
    void should_return_ok_when_projects_are_listed() throws Exception {
        var newest = new ProjectResponse(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "Newest Project",
                "Jane Doe",
                "Active",
                OffsetDateTime.parse("2026-01-02T10:15:30Z"));
        var older = new ProjectResponse(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "Older Project",
                "John Doe",
                "Blocked",
                OffsetDateTime.parse("2026-01-01T10:15:30Z"));

        when(projectService.findAll()).thenReturn(List.of(newest, older));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("33333333-3333-3333-3333-333333333333"))
                .andExpect(jsonPath("$[0].name").value("Newest Project"))
                .andExpect(jsonPath("$[1].id").value("44444444-4444-4444-4444-444444444444"))
                .andExpect(jsonPath("$[1].name").value("Older Project"));
    }

    @Test
    void should_return_ok_when_dashboard_summary_is_requested() throws Exception {
        when(projectService.getDashboardSummary()).thenReturn(new DashboardSummary(6L, 3L, 2L, 1L, 0L));

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(6))
                .andExpect(jsonPath("$.active").value(3))
                .andExpect(jsonPath("$.atRisk").value(2))
                .andExpect(jsonPath("$.blocked").value(1))
                .andExpect(jsonPath("$.onHold").value(0));
    }

    @Test
    void should_return_bad_request_when_name_is_blank() throws Exception {
        var request = new CreateProjectRequest("", "Jane Doe", ProjectStatus.ACTIVE);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name is required"));
    }

    @Test
    void should_return_bad_request_when_owner_name_is_blank() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "", ProjectStatus.ACTIVE);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ownerName is required"));
    }

    @Test
    void should_return_bad_request_when_name_is_too_long() throws Exception {
        var request = new CreateProjectRequest("a".repeat(101), "Jane Doe", ProjectStatus.ACTIVE);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name must be at most 100 characters"));
    }

    @Test
    void should_return_bad_request_when_owner_name_is_too_long() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "a".repeat(101), ProjectStatus.ACTIVE);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ownerName must be at most 100 characters"));
    }

    @Test
    void should_return_bad_request_when_status_is_invalid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"INVALID"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status must be one of Active, At Risk, Blocked, On Hold"));
    }

    @Test
    void should_return_bad_request_when_json_is_malformed() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("request body is malformed"));
    }

    @Test
    void should_return_bad_request_when_id_is_not_a_uuid() throws Exception {
        mockMvc.perform(get("/api/projects/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("id must be a valid UUID"));
    }

    @Test
    void should_return_not_found_when_project_does_not_exist() throws Exception {
        var id = UUID.fromString("55555555-5555-5555-5555-555555555555");
        when(projectService.getById(id)).thenThrow(new ProjectNotFoundException("project not found for id 55555555-5555-5555-5555-555555555555"));

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("project not found for id 55555555-5555-5555-5555-555555555555"));
    }

    @Test
    void should_return_conflict_when_duplicate_name_exists() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        when(projectService.create(any(CreateProjectRequest.class)))
                .thenThrow(new DuplicateProjectNameException("project name already exists: Atlas Migration"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("project name already exists: Atlas Migration"));
    }
}
