package com.edgarkirk.projectpulse;

import static org.assertj.core.api.Assertions.assertThat;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ErrorResponse;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProjectApiAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProjectRepository projectRepository;

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        projectRepository.deleteAll();
    }


    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }

    @Test
    void should_create_and_retrieve_project_via_http() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");

        var createResponse = restTemplate.postForEntity(baseUrl("/api/projects"), request, ProjectResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        var created = createResponse.getBody();
        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Atlas Migration");
        assertThat(created.ownerName()).isEqualTo("Jane Doe");
        assertThat(created.status()).isEqualTo("Active");
        assertThat(created.createdAt()).isNotNull();

        var getResponse = restTemplate.getForEntity(baseUrl("/api/projects/{id}"), ProjectResponse.class, created.id());

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        var retrieved = getResponse.getBody();
        assertThat(retrieved.id()).isEqualTo(created.id());
        assertThat(retrieved.name()).isEqualTo(created.name());
        assertThat(retrieved.ownerName()).isEqualTo(created.ownerName());
        assertThat(retrieved.status()).isEqualTo(created.status());
        assertThat(retrieved.createdAt()).isNotNull();
    }

    @Test
    void should_return_conflict_when_duplicate_project_name_is_submitted_via_http() {
        var original = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");
        var duplicate = new CreateProjectRequest("atlas migration", "John Doe", "Blocked");

        var firstResponse = restTemplate.postForEntity(baseUrl("/api/projects"), original, ProjectResponse.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var duplicateResponse = restTemplate.postForEntity(baseUrl("/api/projects"), duplicate, ErrorResponse.class);

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicateResponse.getBody()).isNotNull();
        assertThat(duplicateResponse.getBody().message()).contains("already taken");
    }

    @Test
    void should_return_bad_request_when_create_request_is_invalid_via_http() {
        var request = new CreateProjectRequest("", "Jane Doe", "Active");

        var response = restTemplate.postForEntity(baseUrl("/api/projects"), request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("Project name is required");
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
