package com.edgarkirk.projectpulse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.service.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.ProjectNotFoundException;
import com.edgarkirk.projectpulse.service.ProjectService;

@WebMvcTest(ProjectController.class)
@Import(GlobalExceptionHandler.class)
class ProjectControllerTest {

    private static final String PROJECTS_ENDPOINT = "/api/projects";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void should_create_project_successfully_when_request_is_valid() throws Exception {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Instant createdAt = Instant.parse("2026-07-03T09:20:00Z");
        when(projectService.createProject(any())).thenReturn(new ProjectResponse(id, "Atlas Migration", "Jane Doe", "Active", createdAt));

        mockMvc.perform(post(PROJECTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").value("2026-07-03T09:20:00Z"));
    }

    @Test
    void should_return_conflict_when_project_name_is_duplicate() throws Exception {
        when(projectService.createProject(any())).thenThrow(new DuplicateProjectNameException("Project name is already taken"));

        mockMvc.perform(post(PROJECTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Project name is already taken"));
    }

    @Test
    void should_return_bad_request_when_name_is_blank() throws Exception {
        mockMvc.perform(post(PROJECTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("name is required"));
    }

    @Test
    void should_return_bad_request_when_name_is_too_long() throws Exception {
        String longName = "A".repeat(101);

        mockMvc.perform(post(PROJECTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","ownerName":"Jane Doe","status":"Active"}
                                """.formatted(longName)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("name must be at most 100 characters"));
    }

    @Test
    void should_return_bad_request_when_owner_name_is_too_long() throws Exception {
        String longOwnerName = "B".repeat(101);

        mockMvc.perform(post(PROJECTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"%s","status":"Active"}
                                """.formatted(longOwnerName)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ownerName must be at most 100 characters"));
    }

    @Test
    void should_return_bad_request_when_status_is_invalid() throws Exception {
        mockMvc.perform(post(PROJECTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Delayed"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("status must be one of Active, At Risk, Blocked, On Hold"));
    }

    @Test
    void should_return_projects_when_listing_all_projects() throws Exception {
        Instant newer = Instant.parse("2026-07-03T10:00:00Z");
        Instant older = Instant.parse("2026-07-03T09:00:00Z");
        when(projectService.listProjects()).thenReturn(List.of(
                new ProjectResponse(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), "Second", "Owner Two", "Blocked", newer),
                new ProjectResponse(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), "First", "Owner One", "Active", older)));

        mockMvc.perform(get(PROJECTS_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Second"))
                .andExpect(jsonPath("$[1].name").value("First"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-07-03T10:00:00Z"))
                .andExpect(jsonPath("$[1].createdAt").value("2026-07-03T09:00:00Z"));
    }

    @Test
    void should_return_project_when_id_exists() throws Exception {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        when(projectService.getProjectById(id)).thenReturn(new ProjectResponse(id, "Atlas Migration", "Jane Doe", "Active", Instant.parse("2026-07-03T09:20:00Z")));

        mockMvc.perform(get(PROJECTS_ENDPOINT + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"));
    }

    @Test
    void should_return_not_found_when_project_id_is_missing() throws Exception {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        when(projectService.getProjectById(id)).thenThrow(new ProjectNotFoundException("Project with id %s was not found".formatted(id)));

        mockMvc.perform(get(PROJECTS_ENDPOINT + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Project with id aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa was not found"));
    }
}
