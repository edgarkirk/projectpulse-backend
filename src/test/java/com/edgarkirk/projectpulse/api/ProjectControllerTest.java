package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.service.ProjectService;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ProjectController.class, DashboardController.class})
@Import(ApiExceptionHandler.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    void should_returnCreated_when_createRequestIsValid() throws Exception {
        when(projectService.createProject(org.mockito.ArgumentMatchers.any())).thenReturn(
                new ProjectResponse(UUID.randomUUID(), "Atlas Migration", "Jane Doe", "Active", OffsetDateTime.now())
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void should_returnConflict_when_duplicateProjectNameIsRejected() throws Exception {
        when(projectService.createProject(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DuplicateProjectNameException("Project name 'Atlas Migration' is already taken"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Project name 'Atlas Migration' is already taken"));
    }

    @Test
    void should_return404_when_projectIsMissing() throws Exception {
        when(projectService.getProjectById(UUID.fromString("11111111-1111-1111-1111-111111111111")))
                .thenThrow(new ProjectNotFoundException("Project '11111111-1111-1111-1111-111111111111' was not found"));

        mockMvc.perform(get("/api/projects/{id}", UUID.fromString("11111111-1111-1111-1111-111111111111")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project '11111111-1111-1111-1111-111111111111' was not found"));
    }

    @Test
    void should_return400_when_createRequestIsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name is required")));
    }

    @Test
    void should_returnProjects_when_listRequested() throws Exception {
        when(projectService.listProjects()).thenReturn(List.of(
                new ProjectResponse(UUID.randomUUID(), "Gamma", "Owner C", "Active", OffsetDateTime.now()),
                new ProjectResponse(UUID.randomUUID(), "Beta", "Owner B", "Blocked", OffsetDateTime.now()),
                new ProjectResponse(UUID.randomUUID(), "Alpha", "Owner A", "At Risk", OffsetDateTime.now())
        ));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gamma"))
                .andExpect(jsonPath("$[1].name").value("Beta"))
                .andExpect(jsonPath("$[2].name").value("Alpha"));
    }

    @Test
    void should_returnDashboardSummary_when_summaryRequested() throws Exception {
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
