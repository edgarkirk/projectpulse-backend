package com.edgarkirk.projectpulse.api;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.TestCreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.request.TestProjectStatus;
import com.edgarkirk.projectpulse.api.dto.response.TestDashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.TestProjectResponse;
import com.edgarkirk.projectpulse.service.TestProjectService;
import com.edgarkirk.projectpulse.service.exception.TestDuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.TestProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestProjectApiController.class)
@Import(TestProjectApiController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TestProjectService service;

    @Test
    void should_return_created_when_valid_request() throws Exception {
        var request = new TestCreateProjectRequest("Atlas Migration", "Jane Doe", TestProjectStatus.ACTIVE);
        var response = new TestProjectResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Atlas Migration",
                "Jane Doe",
                TestProjectStatus.ACTIVE,
                OffsetDateTime.of(2026, 7, 6, 10, 0, 0, 0, ZoneOffset.UTC));
        when(service.create(any(TestCreateProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"));
    }

    @Test
    void should_return_bad_request_when_name_is_blank() throws Exception {
        var request = new TestCreateProjectRequest("", "Jane Doe", TestProjectStatus.ACTIVE);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_ok_when_projects_are_listed() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                new TestProjectResponse(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        "Newest",
                        "Jane Doe",
                        TestProjectStatus.ACTIVE,
                        OffsetDateTime.of(2026, 7, 6, 11, 0, 0, 0, ZoneOffset.UTC))));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Newest"));
    }

    @Test
    void should_return_not_found_when_project_does_not_exist() throws Exception {
        when(service.findById(UUID.fromString("22222222-2222-2222-2222-222222222222")))
                .thenThrow(new TestProjectNotFoundException("Project not found"));

        mockMvc.perform(get("/api/projects/22222222-2222-2222-2222-222222222222"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project not found"));
    }

    @Test
    void should_return_conflict_when_duplicate_name_is_submitted() throws Exception {
        var request = new TestCreateProjectRequest("Atlas Migration", "Jane Doe", TestProjectStatus.ACTIVE);
        when(service.create(any(TestCreateProjectRequest.class)))
                .thenThrow(new TestDuplicateProjectNameException("Project name Atlas Migration is already taken"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Project name Atlas Migration is already taken"));
    }

    @Test
    void should_return_bad_request_when_json_is_malformed() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void should_return_bad_request_when_status_value_is_invalid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Done"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void should_return_ok_when_dashboard_summary_is_requested() throws Exception {
        when(service.getDashboardSummary()).thenReturn(new TestDashboardSummary(6, 3, 2, 1, 0));

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(6))
                .andExpect(jsonPath("$.active").value(3))
                .andExpect(jsonPath("$.atRisk").value(2))
                .andExpect(jsonPath("$.blocked").value(1))
                .andExpect(jsonPath("$.onHold").value(0));
    }
}
