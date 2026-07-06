package com.edgarkirk.projectpulse.persistence;

import java.lang.reflect.Constructor;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.edgarkirk.projectpulse.api.dto.request.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository repository;

    @Test
    void should_find_all_by_order_by_created_at_desc_when_projects_exist() {
        repository.saveAndFlush(project(
                "First",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                OffsetDateTime.of(2026, 7, 1, 8, 0, 0, 0, ZoneOffset.UTC)));
        repository.saveAndFlush(project(
                "Second",
                "John Doe",
                ProjectStatus.BLOCKED,
                OffsetDateTime.of(2026, 7, 2, 8, 0, 0, 0, ZoneOffset.UTC)));

        var results = repository.findAllByOrderByCreatedAtDesc();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("Second");
        assertThat(results.get(1).getName()).isEqualTo("First");
    }

    @Test
    void should_return_true_when_name_exists_case_insensitively() {
        repository.saveAndFlush(project(
                "Atlas Migration",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                OffsetDateTime.now(ZoneOffset.UTC)));

        assertThat(repository.existsByNameIgnoreCase("atlas migration")).isTrue();
    }

    @Test
    void should_count_projects_by_status_when_multiple_statuses_exist() {
        repository.saveAndFlush(project(
                "Active Project",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                OffsetDateTime.now(ZoneOffset.UTC)));
        repository.saveAndFlush(project(
                "At Risk Project",
                "Jane Doe",
                ProjectStatus.AT_RISK,
                OffsetDateTime.now(ZoneOffset.UTC)));
        repository.saveAndFlush(project(
                "Blocked Project",
                "Jane Doe",
                ProjectStatus.BLOCKED,
                OffsetDateTime.now(ZoneOffset.UTC)));

        assertThat(repository.count()).isEqualTo(3);
        assertThat(repository.countByStatus(ProjectStatus.ACTIVE)).isEqualTo(1);
        assertThat(repository.countByStatus(ProjectStatus.AT_RISK)).isEqualTo(1);
        assertThat(repository.countByStatus(ProjectStatus.BLOCKED)).isEqualTo(1);
    }

    @Test
    void should_throw_data_integrity_violation_when_name_is_duplicated() {
        repository.saveAndFlush(project(
                "Atlas Migration",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                OffsetDateTime.now(ZoneOffset.UTC)));

        assertThatThrownBy(() -> repository.saveAndFlush(project(
                "Atlas Migration",
                "John Doe",
                ProjectStatus.BLOCKED,
                OffsetDateTime.now(ZoneOffset.UTC))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Project project(String name, String ownerName, ProjectStatus status, OffsetDateTime createdAt) {
        try {
            Constructor<Project> constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Project project = constructor.newInstance();
            project.setName(name);
            project.setOwnerName(ownerName);
            project.setStatus(status);
            project.setCreatedAt(createdAt);
            return project;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create project", e);
        }
    }
}
