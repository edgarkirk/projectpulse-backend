package com.edgarkirk.projectpulse.mapper;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.domain.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectMapperTest {

    private final ProjectMapper projectMapper = new ProjectMapper();

    @Test
    void should_map_request_to_entity_when_request_is_valid() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);

        Project entity = projectMapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("Atlas Migration");
        assertThat(entity.getOwnerName()).isEqualTo("Jane Doe");
        assertThat(entity.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    void should_map_entity_to_response_when_entity_is_persisted() {
        var entity = org.springframework.beans.BeanUtils.instantiateClass(Project.class);
        entity.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        entity.setName("Atlas Migration");
        entity.setOwnerName("Jane Doe");
        entity.setStatus(ProjectStatus.ACTIVE);
        entity.setCreatedAt(Instant.parse("2026-01-01T10:15:30Z"));

        ProjectResponse response = projectMapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(entity.getId());
        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
        assertThat(response.status()).isEqualTo("Active");
        assertThat(response.createdAt()).isEqualTo(entity.getCreatedAt());
    }
}
