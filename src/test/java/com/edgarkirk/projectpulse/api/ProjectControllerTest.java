package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void clearData() {
        projectRepository.deleteAll();
    }

    @Test
    void createProjectReturnsCreatedProject() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.ownerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createProjectRejectsDuplicateNameIgnoringCase() throws Exception {
        projectRepository.save(new Project("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE));

        var request = new CreateProjectRequest("atlas migration", "Someone Else", ProjectStatus.BLOCKED);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Project name atlas migration is already taken"));
    }

    @Test
    void listProjectsReturnsNewestFirst() throws Exception {
        projectRepository.save(new Project("Older", "Owner 1", ProjectStatus.ACTIVE));
        Thread.sleep(5);
        projectRepository.save(new Project("Newer", "Owner 2", ProjectStatus.BLOCKED));
        projectRepository.flush();

        assertThat(projectRepository.findAllByOrderByCreatedAtDesc())
                .extracting(Project::getName)
                .containsExactly("Newer", "Older");

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Newer"))
                .andExpect(jsonPath("$[1].name").value("Older"));
    }

    @Test
    void getProjectReturnsProjectWhenItExists() throws Exception {
        Project saved = projectRepository.save(new Project("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE));

        mockMvc.perform(get("/api/projects/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.name").value("Atlas Migration"))
                .andExpect(jsonPath("$.status").value("Active"));
    }

    @Test
    void getProjectReturnsNotFoundForMissingId() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project not found"));
    }

    @Test
    void dashboardSummaryReflectsCurrentProjectCounts() throws Exception {
        projectRepository.saveAll(List.of(
                new Project("Active 1", "Owner 1", ProjectStatus.ACTIVE),
                new Project("Active 2", "Owner 2", ProjectStatus.ACTIVE),
                new Project("Active 3", "Owner 3", ProjectStatus.ACTIVE),
                new Project("Risk 1", "Owner 4", ProjectStatus.AT_RISK),
                new Project("Risk 2", "Owner 5", ProjectStatus.AT_RISK),
                new Project("Blocked 1", "Owner 6", ProjectStatus.BLOCKED)));
        projectRepository.flush();

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(6))
                .andExpect(jsonPath("$.active").value(3))
                .andExpect(jsonPath("$.atRisk").value(2))
                .andExpect(jsonPath("$.blocked").value(1))
                .andExpect(jsonPath("$.onHold").value(0));
    }

    @Test
    void createProjectReturnsValidationMessageForMissingName() throws Exception {
        var payload = "{\"name\":\"\",\"ownerName\":\"Jane Doe\",\"status\":\"Active\"}";

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project name is required"));
    }

    @Test
    void createProjectReturnsValidationMessageForInvalidStatus() throws Exception {
        var payload = "{\"name\":\"Atlas Migration\",\"ownerName\":\"Jane Doe\",\"status\":\"Paused\"}";

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Status is invalid"));
    }
}
