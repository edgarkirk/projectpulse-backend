package com.edgarkirk.projectpulse;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ErrorResponse;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.domain.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProjectAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }

    @Test
    void should_create_and_retrieve_project() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);

        ResponseEntity<ProjectResponse> created = restTemplate.postForEntity(url("/api/projects"), request, ProjectResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().name()).isEqualTo("Atlas Migration");
        assertThat(created.getBody().ownerName()).isEqualTo("Jane Doe");
        assertThat(created.getBody().status()).isEqualTo("Active");
        assertThat(created.getBody().id()).isNotNull();
        assertThat(created.getBody().createdAt()).isNotNull();

        ResponseEntity<ProjectResponse> fetched = restTemplate.getForEntity(url("/api/projects/" + created.getBody().id()), ProjectResponse.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().id()).isEqualTo(created.getBody().id());
        assertThat(fetched.getBody().name()).isEqualTo("Atlas Migration");
    }

    @Test
    void should_return_conflict_when_duplicate_name_exists_case_insensitively() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        restTemplate.postForEntity(url("/api/projects"), request, ProjectResponse.class);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(url("/api/projects"), new CreateProjectRequest("Atlas Migration", "John Doe", ProjectStatus.BLOCKED), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("project name already exists: Atlas Migration");
    }

    @Test
    void should_return_projects_ordered_newest_first() {
        restTemplate.postForEntity(url("/api/projects"), new CreateProjectRequest("Older Project", "Jane Doe", ProjectStatus.BLOCKED), ProjectResponse.class);
        restTemplate.postForEntity(url("/api/projects"), new CreateProjectRequest("Newest Project", "Jane Doe", ProjectStatus.ACTIVE), ProjectResponse.class);

        ResponseEntity<ProjectResponse[]> response = restTemplate.getForEntity(url("/api/projects"), ProjectResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()[0].name()).isEqualTo("Newest Project");
        assertThat(response.getBody()[1].name()).isEqualTo("Older Project");
    }

    @Test
    void should_return_dashboard_summary_counts() {
        restTemplate.postForEntity(url("/api/projects"), new CreateProjectRequest("Active Project One", "Jane Doe", ProjectStatus.ACTIVE), ProjectResponse.class);
        restTemplate.postForEntity(url("/api/projects"), new CreateProjectRequest("At Risk Project One", "Jane Doe", ProjectStatus.AT_RISK), ProjectResponse.class);
        restTemplate.postForEntity(url("/api/projects"), new CreateProjectRequest("Active Project Two", "Jane Doe", ProjectStatus.ACTIVE), ProjectResponse.class);
        restTemplate.postForEntity(url("/api/projects"), new CreateProjectRequest("Blocked Project", "Jane Doe", ProjectStatus.BLOCKED), ProjectResponse.class);
        restTemplate.postForEntity(url("/api/projects"), new CreateProjectRequest("Active Project Three", "Jane Doe", ProjectStatus.ACTIVE), ProjectResponse.class);
        restTemplate.postForEntity(url("/api/projects"), new CreateProjectRequest("Second At Risk Project", "Jane Doe", ProjectStatus.AT_RISK), ProjectResponse.class);

        ResponseEntity<DashboardSummary> response = restTemplate.getForEntity(url("/api/dashboard/summary"), DashboardSummary.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new DashboardSummary(6L, 3L, 2L, 1L, 0L));
    }

    @Test
    void should_return_bad_request_when_name_is_blank() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(url("/api/projects"), new CreateProjectRequest("", "Jane Doe", ProjectStatus.ACTIVE), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("name is required");
    }

    @Test
    void should_return_bad_request_when_name_is_too_long() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(url("/api/projects"), new CreateProjectRequest("a".repeat(101), "Jane Doe", ProjectStatus.ACTIVE), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("name must be at most 100 characters");
    }

    @Test
    void should_return_bad_request_when_status_is_invalid() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>("""
                {"name":"Atlas Migration","ownerName":"Jane Doe","status":"INVALID"}
                """, headers);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(url("/api/projects"), HttpMethod.POST, request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("status must be one of Active, At Risk, Blocked, On Hold");
    }

    @Test
    void should_return_not_found_when_project_does_not_exist() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(url("/api/projects/11111111-1111-1111-1111-111111111111"), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("project not found for id 11111111-1111-1111-1111-111111111111");
    }

    private String url(String path) {
        return URI.create("http://localhost:" + port + path).toString();
    }
}
