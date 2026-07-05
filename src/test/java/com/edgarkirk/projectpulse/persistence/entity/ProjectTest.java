package com.edgarkirk.projectpulse.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void should_consider_projects_equal_when_identifiers_match() {
        var first = new Project();
        first.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        first.setName("Atlas Migration");
        first.setOwnerName("Jane Doe");
        first.setStatus(ProjectStatus.ACTIVE);

        var second = new Project();
        second.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        second.setName("Apollo Migration");
        second.setOwnerName("John Doe");
        second.setStatus(ProjectStatus.BLOCKED);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void should_not_consider_projects_equal_when_identifiers_differ() {
        var first = new Project();
        first.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));

        var second = new Project();
        second.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"));

        assertThat(first).isNotEqualTo(second);
    }
}
