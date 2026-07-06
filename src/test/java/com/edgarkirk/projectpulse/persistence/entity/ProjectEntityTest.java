package com.edgarkirk.projectpulse.persistence.entity;

import com.edgarkirk.projectpulse.domain.ProjectStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectEntityTest {

    @Test
    void should_compare_projects_by_identifier_only() {
        var first = new Project();
        first.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        first.setName("Atlas Migration");
        first.setOwnerName("Jane Doe");
        first.setStatus(ProjectStatus.ACTIVE);

        var second = new Project();
        second.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        second.setName("Other Project");
        second.setOwnerName("John Doe");
        second.setStatus(ProjectStatus.BLOCKED);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }
}
