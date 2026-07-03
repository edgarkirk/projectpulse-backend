package com.edgarkirk.projectpulse.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }

    @Test
    void should_returnCreatedAndProjectBody_when_validProjectIsCreated() throws Exception {
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
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void should_returnConflict_when_duplicateProjectNameExists() throws Exception {
        projectRepository.save(new Project("Atlas Migration", "Existing Owner", "Active", Instant.parse("2026-07-01T10:00:00Z")));

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
    void should_returnProjectsOrderedByCreatedAtDesc_when_projectsExist() throws Exception {
        projectRepository.saveAll(List.of(
                new Project("Older Project", "Owner One", "Blocked", Instant.parse("2026-07-01T09:00:00Z")),
                new Project("Middle Project", "Owner Three", "At Risk", Instant.parse("2026-07-01T10:00:00Z")),
                new Project("Newest Project", "Owner Two", "Active", Instant.parse("2026-07-01T11:00:00Z"))));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Newest Project"))
                .andExpect(jsonPath("$[1].name").value("Middle Project"))
                .andExpect(jsonPath("$[2].name").value("Older Project"));
    }

    @Test
    void should_returnProject_when_projectExists() throws Exception {
        Project savedProject = projectRepository.save(new Project(
                "Atlas Migration",
                "Jane Doe",
                "Active",
                Instant.parse("2026-07-01T10:00:00Z")));

        mockMvc.perform(get("/api/projects/{id}", savedProject.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProject.getId().toString()))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"));
    }

    @Test
    void should_returnNotFound_when_projectDoesNotExist() throws Exception {
        UUID missingId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        mockMvc.perform(get("/api/projects/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project '11111111-1111-1111-1111-111111111111' was not found."));
    }

    @Test
    void should_returnDashboardSummary_when_databaseHasProjects() throws Exception {
        projectRepository.saveAll(List.of(
                new Project("Active One", "Owner One", "Active", Instant.parse("2026-07-01T09:00:00Z")),
                new Project("Active Two", "Owner Two", "Active", Instant.parse("2026-07-01T09:10:00Z")),
                new Project("Active Three", "Owner Three", "Active", Instant.parse("2026-07-01T09:20:00Z")),
                new Project("At Risk One", "Owner Four", "At Risk", Instant.parse("2026-07-01T09:30:00Z")),
                new Project("At Risk Two", "Owner Five", "At Risk", Instant.parse("2026-07-01T09:40:00Z")),
                new Project("Blocked One", "Owner Six", "Blocked", Instant.parse("2026-07-01T09:50:00Z"))));

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(6))
                .andExpect(jsonPath("$.active").value(3))
                .andExpect(jsonPath("$.atRisk").value(2))
                .andExpect(jsonPath("$.blocked").value(1))
                .andExpect(jsonPath("$.onHold").value(0));
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
