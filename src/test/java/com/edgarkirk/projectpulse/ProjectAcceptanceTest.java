package com.edgarkirk.projectpulse;

import java.lang.reflect.Constructor;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.request.ProjectStatus;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ErrorResponse;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ProjectPulseApplication.class)
@ActiveProfiles("test")
class ProjectAcceptanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }

    @Test
    void should_create_and_retrieve_project_when_request_is_valid() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);

        var created = restTemplate.postForEntity("/api/projects", request, ProjectResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().id()).isNotNull();
        assertThat(created.getBody().name()).isEqualTo("Atlas Migration");
        assertThat(created.getBody().ownerName()).isEqualTo("Jane Doe");
        assertThat(created.getBody().status()).isEqualTo("Active");
        assertThat(created.getBody().createdAt()).isNotNull();

        UUID id = created.getBody().id();
        var fetched = restTemplate.getForEntity("/api/projects/" + id, ProjectResponse.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().id()).isEqualTo(id);
        assertThat(fetched.getBody().name()).isEqualTo("Atlas Migration");
    }

    @Test
    void should_return_not_found_when_project_does_not_exist() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000000");

        var response = restTemplate.getForEntity("/api/projects/" + id, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Project with id 00000000-0000-0000-0000-000000000000 not found");
    }

    @Test
    void should_return_conflict_when_duplicate_name_is_submitted() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        restTemplate.postForEntity("/api/projects", request, ProjectResponse.class);

        var duplicate = restTemplate.postForEntity("/api/projects", request, ErrorResponse.class);

        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicate.getBody()).isNotNull();
        assertThat(duplicate.getBody().message()).isEqualTo("Project name 'Atlas Migration' already exists");
    }

    @Test
    void should_return_bad_request_when_request_is_invalid() {
        var request = new CreateProjectRequest("", "Jane Doe", ProjectStatus.ACTIVE);

        var response = restTemplate.postForEntity("/api/projects", request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("name: name is required");
    }

    @Test
    void should_return_dashboard_summary_when_projects_exist() {
        projectRepository.saveAndFlush(project(
                "Active Project",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                OffsetDateTime.of(2026, 7, 1, 8, 0, 0, 0, ZoneOffset.UTC)));
        projectRepository.saveAndFlush(project(
                "At Risk Project",
                "Jane Doe",
                ProjectStatus.AT_RISK,
                OffsetDateTime.of(2026, 7, 2, 8, 0, 0, 0, ZoneOffset.UTC)));
        projectRepository.saveAndFlush(project(
                "Blocked Project",
                "Jane Doe",
                ProjectStatus.BLOCKED,
                OffsetDateTime.of(2026, 7, 3, 8, 0, 0, 0, ZoneOffset.UTC)));

        var response = restTemplate.getForEntity("/api/dashboard/summary", DashboardSummary.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalProjects()).isEqualTo(3);
        assertThat(response.getBody().active()).isEqualTo(1);
        assertThat(response.getBody().atRisk()).isEqualTo(1);
        assertThat(response.getBody().blocked()).isEqualTo(1);
        assertThat(response.getBody().onHold()).isEqualTo(0);
    }

    @Test
    void should_return_projects_in_descending_created_order_when_listed() {
        projectRepository.saveAndFlush(project(
                "Older Project",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                OffsetDateTime.of(2026, 7, 1, 8, 0, 0, 0, ZoneOffset.UTC)));
        projectRepository.saveAndFlush(project(
                "Newest Project",
                "Jane Doe",
                ProjectStatus.BLOCKED,
                OffsetDateTime.of(2026, 7, 2, 8, 0, 0, 0, ZoneOffset.UTC)));

        var response = restTemplate.getForEntity("/api/projects", ProjectResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()[0].name()).isEqualTo("Newest Project");
        assertThat(response.getBody()[1].name()).isEqualTo("Older Project");
    }

    private Project project(String name, String ownerName, ProjectStatus status, OffsetDateTime createdAt) {
        try {
            Constructor<Project> constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Project project = constructor.newInstance();
            project.setName(name);
            project.setOwnerName(ownerName);
            project.setStatus(status);
            project.setCreatedAt(createdAt);
            return project;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create project", e);
        }
   }
}
