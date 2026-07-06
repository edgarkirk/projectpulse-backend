package com.edgarkirk.projectpulse.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.service.ProjectService;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void should_return_created_when_valid_request() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        var response = new ProjectResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Atlas Migration",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-07-06T12:00:00Z"));

        when(projectService.create(any(CreateProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").value("2026-07-06T12:00:00Z"));
    }

    @Test
    void should_return_ok_when_projects_are_listed() throws Exception {
        var first = new ProjectResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Newest",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-07-06T12:00:00Z"));
        var second = new ProjectResponse(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Older",
                "John Smith",
                ProjectStatus.BLOCKED,
                Instant.parse("2026-07-05T12:00:00Z"));

        when(projectService.listAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Newest"))
                .andExpect(jsonPath("$[1].name").value("Older"));
    }

    @Test
    void should_return_ok_when_project_is_found_by_id() throws Exception {
        var projectId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var response = new ProjectResponse(
                projectId,
                "Atlas Migration",
                "Jane Doe",
                ProjectStatus.AT_RISK,
                Instant.parse("2026-07-06T12:00:00Z"));

        when(projectService.getById(projectId)).thenReturn(response);

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.status").value("At Risk"));
    }

    @Test
    void should_return_ok_when_dashboard_summary_is_requested() throws Exception {
        when(projectService.getDashboardSummary()).thenReturn(new DashboardSummary(6, 3, 2, 1, 0));

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
    void should_return_bad_request_when_name_is_too_long() throws Exception {
        var request = new CreateProjectRequest("a".repeat(101), "Jane Doe", ProjectStatus.ACTIVE);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name exceeds the maximum length of 100 characters"));
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
    void should_return_bad_request_when_owner_name_is_too_long() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "a".repeat(101), ProjectStatus.ACTIVE);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ownerName exceeds the maximum length of 100 characters"));
    }

    @Test
    void should_return_bad_request_when_status_is_missing() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", null);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status is required"));
    }

    @Test
    void should_return_bad_request_when_status_is_invalid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Unsupported"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status must be one of Active, At Risk, Blocked, On Hold"));
    }

    @Test
    void should_return_not_found_when_project_does_not_exist() throws Exception {
        var projectId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(projectService.getById(projectId)).thenThrow(new ProjectNotFoundException("Project not found"));

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project not found"));
    }

    @Test
    void should_return_bad_request_when_project_id_is_not_a_uuid() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("id must be a valid UUID"));
    }


    @Test
    void should_return_conflict_when_duplicate_name_exists() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        when(projectService.create(any(CreateProjectRequest.class)))
                .thenThrow(new DuplicateProjectNameException("Project name already taken"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Project name already taken"));
    }

    @Test
    void should_return_bad_request_when_json_is_malformed() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"));
    }
}
