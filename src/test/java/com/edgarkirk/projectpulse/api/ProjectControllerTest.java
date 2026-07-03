package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.service.ProjectService;
import org.junit.jupiter.api.Test;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void should_returnCreated_when_validInput() throws Exception {
        ProjectResponse response = new ProjectResponse(UUID.randomUUID().toString(), "Atlas Migration", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-03T12:00:00Z"));
        given(projectService.createProject(new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active"))).willReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void should_returnAllItems_when_getAll() throws Exception {
        ProjectResponse response = new ProjectResponse(UUID.randomUUID().toString(), "Atlas Migration", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-03T12:00:00Z"));
        given(projectService.listProjects()).willReturn(List.of(response));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(response.id()));
    }

    @Test
    void should_returnProject_when_projectExists() throws Exception {
        UUID id = UUID.randomUUID();
        ProjectResponse response = new ProjectResponse(id.toString(), "Atlas Migration", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-03T12:00:00Z"));
        given(projectService.getProject(id)).willReturn(response);

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void should_returnBadRequest_when_nameIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project name is required"));
    }

    @Test
    void should_returnBadRequest_when_ownerNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"","status":"Active"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Owner name is required"));
    }

    @Test
    void should_returnBadRequest_when_statusIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project status is required"));
    }


    @Test
    void should_returnBadRequest_when_nameExceedsLimit() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","ownerName":"Jane Doe","status":"Active"}
                                """.formatted("A".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project name must not exceed 100 characters"));
    }

    @Test
    void should_returnBadRequest_when_ownerNameExceedsLimit() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"%s","status":"Active"}
                                """.formatted("A".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Owner name must not exceed 100 characters"));
    }

    @Test
    void should_returnBadRequest_when_statusIsInvalid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Done"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project status must be one of: Active, At Risk, Blocked, On Hold"));
    }

    @Test
    void should_returnConflict_when_duplicateNameExists() throws Exception {
        given(projectService.createProject(new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active")))
                .willThrow(new DuplicateProjectNameException("Project name 'Atlas Migration' is already taken"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Project name 'Atlas Migration' is already taken"));
    }

    @Test
    void should_returnNotFound_when_projectMissing() throws Exception {
        UUID id = UUID.randomUUID();
        given(projectService.getProject(id))
                .willThrow(new ProjectNotFoundException(id));

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project with id %s was not found".formatted(id)));
    }
}
