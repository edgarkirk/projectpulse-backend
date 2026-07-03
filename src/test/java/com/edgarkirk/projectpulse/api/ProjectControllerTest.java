package com.edgarkirk.projectpulse.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.service.ProjectService;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void should_returnCreated_when_validCreateRequestSubmitted() throws Exception {
        ProjectResponse response = new ProjectResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Atlas Migration",
                "Jane Doe",
                "Active",
                Instant.parse("2026-07-01T10:00:00Z"));
        when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Atlas Migration",
                                  "ownerName": "Jane Doe",
                                  "status": "Active"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").value("2026-07-01T10:00:00Z"));
    }

    @Test
    void should_returnProjects_when_projectsRequested() throws Exception {
        when(projectService.getAllProjects()).thenReturn(List.of(
                new ProjectResponse(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Newest Project", "Owner Two", "Active", Instant.parse("2026-07-01T11:00:00Z")),
                new ProjectResponse(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Older Project", "Owner One", "Blocked", Instant.parse("2026-07-01T09:00:00Z"))));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Newest Project"))
                .andExpect(jsonPath("$[1].name").value("Older Project"));
    }

    @Test
    void should_returnProject_when_projectExists() throws Exception {
        UUID projectId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(projectService.getProjectById(projectId)).thenReturn(new ProjectResponse(
                projectId,
                "Atlas Migration",
                "Jane Doe",
                "Active",
                Instant.parse("2026-07-01T10:00:00Z")));

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Atlas Migration"));
    }

    @Test
    void should_returnNotFound_when_projectDoesNotExist() throws Exception {
        UUID projectId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(projectService.getProjectById(projectId)).thenThrow(new ProjectNotFoundException(projectId));

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project '11111111-1111-1111-1111-111111111111' was not found."));
    }

    @Test
    void should_returnConflict_when_duplicateProjectName() throws Exception {
        doThrow(new DuplicateProjectNameException("Atlas Migration"))
                .when(projectService).createProject(any(CreateProjectRequest.class));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Atlas Migration",
                                  "ownerName": "Jane Doe",
                                  "status": "Active"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Project name 'Atlas Migration' is already taken."));
    }

    @Test
    void should_returnBadRequest_when_nameIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "ownerName": "Jane Doe",
                                  "status": "Active"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name is required."));
    }

    @Test
    void should_returnBadRequest_when_nameExceedsMaxLength() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "ownerName": "Jane Doe",
                                  "status": "Active"
                                }
                                """.formatted("A".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name must be at most 100 characters."));
    }

    @Test
    void should_returnBadRequest_when_ownerNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Atlas Migration",
                                  "ownerName": "",
                                  "status": "Active"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ownerName is required."));
    }

    @Test
    void should_returnBadRequest_when_ownerNameExceedsMaxLength() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Atlas Migration",
                                  "ownerName": "%s",
                                  "status": "Active"
                                }
                                """.formatted("A".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ownerName must be at most 100 characters."));
    }

    @Test
    void should_returnBadRequest_when_statusIsInvalid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Atlas Migration",
                                  "ownerName": "Jane Doe",
                                  "status": "Paused"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status must be one of Active, At Risk, Blocked, On Hold."));
    }
}
