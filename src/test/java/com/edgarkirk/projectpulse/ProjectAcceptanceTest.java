package com.edgarkirk.projectpulse;

import static org.assertj.core.api.Assertions.assertThat;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProjectAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_create_and_retrieve_project() throws Exception {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);

        ResponseEntity<String> created = restTemplate.postForEntity(
                baseUrl() + "/api/projects", request, String.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotBlank();

        ProjectResponse createdProject = objectMapper.readValue(created.getBody(), ProjectResponse.class);
        UUID projectId = createdProject.id();
        assertThat(createdProject.name()).isEqualTo("Atlas Migration");

        ResponseEntity<String> fetched = restTemplate.getForEntity(
                baseUrl() + "/api/projects/" + projectId, String.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotBlank();
        ProjectResponse fetchedProject = objectMapper.readValue(fetched.getBody(), ProjectResponse.class);
        assertThat(fetchedProject.id()).isEqualTo(projectId);
    }

    @Test
    void should_return_dashboard_summary_counts() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/api/dashboard/summary", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("totalProjects");
        DashboardSummary summary = objectMapper.readValue(response.getBody(), DashboardSummary.class);
        assertThat(summary.totalProjects()).isEqualTo(6);
        assertThat(summary.active()).isEqualTo(3);
        assertThat(summary.atRisk()).isEqualTo(2);
        assertThat(summary.blocked()).isEqualTo(1);
        assertThat(summary.onHold()).isEqualTo(0);
    }

    @Test
    void should_return_not_found_for_missing_project() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/api/projects/11111111-1111-1111-1111-111111111111", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("message");
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
