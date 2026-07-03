package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.response.DashboardSummaryResponse;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ProjectController.class, DashboardController.class})
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void should_returnCreatedProject_when_validCreateRequest() throws Exception {
        when(projectService.createProject(org.mockito.ArgumentMatchers.any())).thenReturn(
                new ProjectResponse(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        "Atlas Migration",
                        "Jane Doe",
                        "Active",
                        Instant.parse("2026-07-03T10:00:00Z")));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").value("2026-07-03T10:00:00Z"));
    }

    @Test
    void should_return400_when_nameIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name is required")));
    }

    @Test
    void should_return404_when_projectDoesNotExist() throws Exception {
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("not found")));
    }

    @Test
    void should_returnAllProjects_when_listingProjects() throws Exception {
        when(projectService.listProjects()).thenReturn(List.of(
                new ProjectResponse(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Newest", "Owner 3", "Blocked", Instant.parse("2026-07-03T10:00:00Z")),
                new ProjectResponse(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Older", "Owner 2", "At Risk", Instant.parse("2026-07-02T10:00:00Z"))));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Newest"))
                .andExpect(jsonPath("$[1].name").value("Older"));
    }

    @Test
    void should_returnDashboardSummary_when_requested() throws Exception {
        when(projectService.getDashboardSummary()).thenReturn(new DashboardSummaryResponse(6, 3, 2, 1, 0));

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(6))
                .andExpect(jsonPath("$.active").value(3))
                .andExpect(jsonPath("$.atRisk").value(2))
                .andExpect(jsonPath("$.blocked").value(1))
                .andExpect(jsonPath("$.onHold").value(0));
    }
}
