package com.edgarkirk.projectpulse;

import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.TestCreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.request.TestProjectStatus;
import com.edgarkirk.projectpulse.api.dto.response.TestDashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.TestProjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.edgarkirk.projectpulse.TestApplication.class)
@ActiveProfiles("test")
class ProjectAcceptanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_create_and_retrieve_project_when_request_is_valid() {
        var request = new TestCreateProjectRequest("Atlas Migration", "Jane Doe", TestProjectStatus.ACTIVE);

        var created = restTemplate.postForEntity("/api/projects", request, TestProjectResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().id()).isNotNull();
        assertThat(created.getBody().name()).isEqualTo("Atlas Migration");
        assertThat(created.getBody().ownerName()).isEqualTo("Jane Doe");
        assertThat(created.getBody().status()).isEqualTo(TestProjectStatus.ACTIVE);
        assertThat(created.getBody().createdAt()).isNotNull();

        UUID id = created.getBody().id();
        var fetched = restTemplate.getForEntity("/api/projects/" + id, TestProjectResponse.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().id()).isEqualTo(id);
        assertThat(fetched.getBody().name()).isEqualTo("Atlas Migration");
    }

    @Test
    void should_return_not_found_when_project_does_not_exist() {
        var response = restTemplate.getForEntity("/api/projects/00000000-0000-0000-0000-000000000000", TestProjectResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_return_dashboard_summary_when_projects_exist() {
        var response = restTemplate.getForEntity("/api/dashboard/summary", TestDashboardSummary.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalProjects()).isEqualTo(6);
        assertThat(response.getBody().active()).isEqualTo(3);
        assertThat(response.getBody().atRisk()).isEqualTo(2);
        assertThat(response.getBody().blocked()).isEqualTo(1);
        assertThat(response.getBody().onHold()).isEqualTo(0);
    }
}
