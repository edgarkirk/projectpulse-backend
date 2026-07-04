package com.edgarkirk.projectpulse.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.persistence.entity.Project;

class ProjectMapperTest {

    private final ProjectMapper projectMapper = new ProjectMapper();

    @Test
    void should_map_create_request_to_project_entity_when_converting_request() {
        CreateProjectRequest request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");

        Project project = projectMapper.toEntity(request);

        assertThat(project.getName()).isEqualTo("Atlas Migration");
        assertThat(project.getOwnerName()).isEqualTo("Jane Doe");
        assertThat(project.getStatus()).isEqualTo("Active");
    }

    @Test
    void should_map_project_entity_to_response_when_converting_entity() {
        Project project = new Project("Atlas Migration", "Jane Doe", "Active");
        setField(project, "id", UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        setField(project, "createdAt", Instant.parse("2026-07-03T09:20:00Z"));

        var response = projectMapper.toResponse(project);

        assertThat(response.id()).isEqualTo(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
        assertThat(response.status()).isEqualTo("Active");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-07-03T09:20:00Z"));
    }

    private static void setField(Project project, String fieldName, Object value) {
        try {
            var field = Project.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(project, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
