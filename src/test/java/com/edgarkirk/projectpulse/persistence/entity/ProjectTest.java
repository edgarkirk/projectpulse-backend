package com.edgarkirk.projectpulse.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void should_consider_projects_with_same_id_equal_when_compared() {
        Project first = new Project("Atlas Migration", "Jane Doe", "Active");
        Project second = new Project("Other", "Someone Else", "Blocked");

        UUID id = UUID.randomUUID();
        setId(first, id);
        setId(second, id);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }

    private static void setId(Project project, UUID id) {
        try {
            var field = Project.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(project, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
