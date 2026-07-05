package com.edgarkirk.projectpulse.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;

import org.junit.jupiter.api.Test;

class ProjectMapperTest {

    private final ProjectMapper projectMapper = new ProjectMapper();

    @Test
    void should_map_create_request_to_entity_when_request_is_valid() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");

        var entity = projectMapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("Atlas Migration");
        assertThat(entity.getOwnerName()).isEqualTo("Jane Doe");
        assertThat(entity.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    void should_map_entity_to_response_when_entity_is_populated() {
        var entity = newProject();
        entity.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        entity.setName("Atlas Migration");
        entity.setOwnerName("Jane Doe");
        entity.setStatus(ProjectStatus.AT_RISK);
        entity.setCreatedAt(Instant.parse("2024-01-01T10:15:30Z"));

        var response = projectMapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(entity.getId());
        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
        assertThat(response.status()).isEqualTo("At Risk");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2024-01-01T10:15:30Z"));
    }

    @Test
    void should_throw_illegal_argument_exception_when_status_is_invalid() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "In Progress");

        assertThatThrownBy(() -> projectMapper.toEntity(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Project status must be one of Active, At Risk, Blocked, or On Hold");
    }

    private Project newProject() {
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
