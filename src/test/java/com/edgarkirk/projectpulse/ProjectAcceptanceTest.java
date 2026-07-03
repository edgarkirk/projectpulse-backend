package com.edgarkirk.projectpulse;

import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProjectAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }

    @Test
    void should_returnCreated_when_validInput() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType("application/json")
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void should_returnConflict_when_duplicateProjectNameSubmitted() throws Exception {
        insertProject(UUID.randomUUID(), "Atlas Migration", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-03T10:00:00Z"));

        mockMvc.perform(post("/api/projects")
                        .contentType("application/json")
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Project name 'Atlas Migration' is already taken"));
    }

    @Test
    void should_returnProjectsNewestFirst_when_listingProjects() throws Exception {
        insertProject(UUID.randomUUID(), "Old Project", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-01T10:00:00Z"));
        insertProject(UUID.randomUUID(), "New Project", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-03T10:00:00Z"));
        insertProject(UUID.randomUUID(), "Middle Project", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-02T10:00:00Z"));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("New Project"))
                .andExpect(jsonPath("$[1].name").value("Middle Project"))
                .andExpect(jsonPath("$[2].name").value("Old Project"));
    }

    @Test
    void should_returnProject_when_projectExists() throws Exception {
        UUID id = UUID.randomUUID();
        insertProject(id, "Atlas Migration", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-03T10:00:00Z"));

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Atlas Migration"));
    }

    @Test
    void should_returnNotFound_when_projectMissing() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project not found"));
    }

    @Test
    void should_returnDashboardSummary_when_projectsExist() throws Exception {
        insertProject(UUID.randomUUID(), "A", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-01T10:00:00Z"));
        insertProject(UUID.randomUUID(), "B", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-02T10:00:00Z"));
        insertProject(UUID.randomUUID(), "C", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-03T10:00:00Z"));
        insertProject(UUID.randomUUID(), "D", "Jane Doe", "At Risk", OffsetDateTime.parse("2026-07-04T10:00:00Z"));
        insertProject(UUID.randomUUID(), "E", "Jane Doe", "At Risk", OffsetDateTime.parse("2026-07-05T10:00:00Z"));
        insertProject(UUID.randomUUID(), "F", "Jane Doe", "Blocked", OffsetDateTime.parse("2026-07-06T10:00:00Z"));

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
                        .contentType("application/json")
                        .content("""
                                {"name":"","ownerName":"Jane Doe","status":"Active"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project name is required"));
    }


    @Test
    void should_returnBadRequest_when_ownerNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType("application/json")
                        .content("""
                                {"name":"Atlas Migration","ownerName":"","status":"Active"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Owner name is required"));
    }

    @Test
    void should_returnBadRequest_when_statusIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType("application/json")
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project status is required"));
    }

    @Test
    void should_returnBadRequest_when_nameExceeds100Characters() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType("application/json")
                        .content("""
                                {"name":"%s","ownerName":"Jane Doe","status":"Active"}
                                """.formatted("A".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project name must not exceed 100 characters"));
    }

    @Test
    void should_returnBadRequest_when_statusIsInvalid() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType("application/json")
                        .content("""
                                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"Done"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project status must be one of: Active, At Risk, Blocked, On Hold"));
    }

    private void insertProject(UUID id, String name, String ownerName, String status, OffsetDateTime createdAt) {
        jdbcTemplate.update("insert into projects (id, name, owner_name, status, created_at) values (?, ?, ?, ?, ?)",
                id,
                name,
                ownerName,
                status,
                Timestamp.from(createdAt.toInstant()));
    }
}
