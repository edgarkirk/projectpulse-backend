package com.edgarkirk.projectpulse.api;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.edgarkirk.projectpulse.ProjectPulseApplication;
import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.request.ProjectStatus;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.service.ProjectService;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ProjectPulseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
                "Active",
                OffsetDateTime.of(2026, 7, 6, 10, 0, 0, 0, ZoneOffset.UTC));
        when(projectService.create(any(CreateProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").value("2026-07-06T10:00:00Z"));
    }

    @Test
    void should_return_bad_request_when_name_is_blank() throws Exception {
        var request = new CreateProjectRequest("", "Jane Doe", ProjectStatus.ACTIVE);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: name is required"));
    }

    @Test
    void should_return_ok_when_projects_are_listed() throws Exception {
        when(projectService.findAll()).thenReturn(List.of(
                new ProjectResponse(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        "Newest",
                        "Jane Doe",
                        "Active",
                        OffsetDateTime.of(2026, 7, 6, 11, 0, 0, 0, ZoneOffset.UTC)),
                new ProjectResponse(
                        UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        "Older",
                        "John Doe",
                        "Blocked",
                        OffsetDateTime.of(2026, 7, 6, 9, 0, 0, 0, ZoneOffset.UTC))));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Newest"))
                .andExpect(jsonPath("$[1].name").value("Older"));
    }

    @Test
    void should_return_not_found_when_project_does_not_exist() throws Exception {
        var id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(projectService.findById(id)).thenThrow(new ProjectNotFoundException("Project with id 22222222-2222-2222-2222-222222222222 not found"));

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project with id 22222222-2222-2222-2222-222222222222 not found"));
    }

    @Test
    void should_return_conflict_when_duplicate_name_is_submitted() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        when(projectService.create(any(CreateProjectRequest.class)))
                .thenThrow(new DuplicateProjectNameException("Project name 'Atlas Migration' already exists"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Project name 'Atlas Migration' already exists"));
    }

    @Test
    void should_return_bad_request_when_json_is_malformed() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"));
    }

    @Test
    void should_return_bad_request_when_status_value_is_invalid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Done"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status must be one of Active, At Risk, Blocked, On Hold"));
    }

    @Test
    void should_return_bad_request_when_path_variable_is_invalid() throws Exception {
        mockMvc.perform(get("/api/projects/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
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
}
