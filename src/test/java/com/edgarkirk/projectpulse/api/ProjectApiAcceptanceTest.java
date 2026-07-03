package com.edgarkirk.projectpulse.api;

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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
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
    void should_returnCreated_when_validInput() throws Exception {
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
    void should_returnConflict_when_duplicateNameExists() throws Exception {
        persistProject("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already taken")));
    }

    @Test
    void should_return400_when_nameMissing() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name is required")));
    }

    @Test
    void should_return400_when_nameTooLong() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","ownerName":"Jane Doe","status":"Active"}
                                """.formatted("A".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name exceeds maximum length of 100")));
    }

    @Test
    void should_return400_when_ownerNameTooLong() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"%s","status":"Active"}
                                """.formatted("A".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ownerName exceeds maximum length of 100")));
    }

    @Test
    void should_return400_when_statusInvalid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Paused"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("status must be one of Active, At Risk, Blocked, On Hold")));
    }

    @Test
    void should_returnAllProjectsOrderedNewestFirst_when_listingProjects() throws Exception {
        persistProject("Alpha", "Owner A", ProjectStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC).minusDays(3));
        persistProject("Beta", "Owner B", ProjectStatus.BLOCKED, OffsetDateTime.now(ZoneOffset.UTC).minusDays(2));
        persistProject("Gamma", "Owner C", ProjectStatus.AT_RISK, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value("Gamma"))
                .andExpect(jsonPath("$.[1].name").value("Beta"))
                .andExpect(jsonPath("$.[2].name").value("Alpha"));
    }

    @Test
    void should_returnProject_when_projectExists() throws Exception {
        Project project = persistProject("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));

        mockMvc.perform(get("/api/projects/{id}", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(project.getId().toString()))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void should_return404_when_projectMissing() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("not found")));
    }

    @Test
    void should_returnDashboardSummary_when_statusCountsExist() throws Exception {
        persistProject("Alpha", "Owner A", ProjectStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC).minusDays(4));
        persistProject("Beta", "Owner B", ProjectStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC).minusDays(3));
        persistProject("Gamma", "Owner C", ProjectStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC).minusDays(2));
        persistProject("Delta", "Owner D", ProjectStatus.AT_RISK, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        persistProject("Epsilon", "Owner E", ProjectStatus.AT_RISK, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        persistProject("Zeta", "Owner F", ProjectStatus.BLOCKED, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(6))
                .andExpect(jsonPath("$.active").value(3))
                .andExpect(jsonPath("$.atRisk").value(2))
                .andExpect(jsonPath("$.blocked").value(1))
                .andExpect(jsonPath("$.onHold").value(0));
    }

    @Test
    void should_not_requireAuthentication_when_accessingProjectEndpoints() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(401))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(403));
    }

    private Project persistProject(String name, String ownerName, ProjectStatus status, OffsetDateTime createdAt) {
        Project project = new Project();
        project.setName(name);
        project.setOwnerName(ownerName);
        project.setStatus(status);
        project.setCreatedAt(createdAt);
        return projectRepository.saveAndFlush(project);
    }
}
