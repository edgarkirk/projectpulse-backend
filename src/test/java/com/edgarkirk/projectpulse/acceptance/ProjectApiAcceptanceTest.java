package com.edgarkirk.projectpulse.acceptance;

import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectApiAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }

    @Test
    void should_returnCreatedProject_when_validInput() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void should_return409_when_duplicateNameExists() throws Exception {
        projectRepository.save(new Project(UUID.randomUUID(), "Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, Instant.parse("2026-07-01T10:00:00Z")));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already taken")));
    }

    @Test
    void should_returnAllProjectsNewestFirst_when_listingProjects() throws Exception {
        Project oldest = projectRepository.save(new Project(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Oldest", "Owner 1", ProjectStatus.ACTIVE, Instant.parse("2026-07-01T10:00:00Z")));
        Project middle = projectRepository.save(new Project(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Middle", "Owner 2", ProjectStatus.AT_RISK, Instant.parse("2026-07-02T10:00:00Z")));
        Project newest = projectRepository.save(new Project(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Newest", "Owner 3", ProjectStatus.BLOCKED, Instant.parse("2026-07-03T10:00:00Z")));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(newest.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(middle.getId().toString()))
                .andExpect(jsonPath("$[2].id").value(oldest.getId().toString()));
    }

    @Test
    void should_returnProject_when_idExists() throws Exception {
        Project project = projectRepository.save(new Project(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, Instant.parse("2026-07-03T10:00:00Z")));

        mockMvc.perform(get("/api/projects/{id}", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(project.getId().toString()));
    }

    @Test
    void should_return404_when_projectDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", UUID.fromString("22222222-2222-2222-2222-222222222222")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("not found")));
    }

    @Test
    void should_returnDashboardSummary_when_dataExists() throws Exception {
        projectRepository.save(new Project(UUID.randomUUID(), "Active 1", "Owner", ProjectStatus.ACTIVE, Instant.parse("2026-07-01T10:00:00Z")));
        projectRepository.save(new Project(UUID.randomUUID(), "Active 2", "Owner", ProjectStatus.ACTIVE, Instant.parse("2026-07-01T11:00:00Z")));
        projectRepository.save(new Project(UUID.randomUUID(), "Active 3", "Owner", ProjectStatus.ACTIVE, Instant.parse("2026-07-01T12:00:00Z")));
        projectRepository.save(new Project(UUID.randomUUID(), "At Risk 1", "Owner", ProjectStatus.AT_RISK, Instant.parse("2026-07-01T13:00:00Z")));
        projectRepository.save(new Project(UUID.randomUUID(), "At Risk 2", "Owner", ProjectStatus.AT_RISK, Instant.parse("2026-07-01T14:00:00Z")));
        projectRepository.save(new Project(UUID.randomUUID(), "Blocked 1", "Owner", ProjectStatus.BLOCKED, Instant.parse("2026-07-01T15:00:00Z")));

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(6))
                .andExpect(jsonPath("$.active").value(3))
                .andExpect(jsonPath("$.atRisk").value(2))
                .andExpect(jsonPath("$.blocked").value(1))
                .andExpect(jsonPath("$.onHold").value(0));
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
    void should_return400_when_nameIsTooLong() throws Exception {
        String longName = "a".repeat(101);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","ownerName":"Jane Doe","status":"Active"}
                                """.formatted(longName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name must be at most 100 characters")));
    }

    @Test
    void should_return400_when_ownerNameIsTooLong() throws Exception {
        String longOwnerName = "b".repeat(101);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"%s","status":"Active"}
                                """.formatted(longOwnerName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ownerName must be at most 100 characters")));
    }

    @Test
    void should_return400_when_statusIsInvalid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Deferred"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("status must be one of Active, At Risk, Blocked, On Hold")));
    }
}
