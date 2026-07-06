package com.edgarkirk.projectpulse.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void should_find_all_by_order_by_created_at_desc() {
        var older = new Project(
                null,
                "Older",
                "Jane Doe",
                ProjectStatus.BLOCKED,
                Instant.parse("2026-07-05T12:00:00Z"));
        var newer = new Project(
                null,
                "Newer",
                "John Smith",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-07-06T12:00:00Z"));
        projectRepository.save(older);
        projectRepository.save(newer);
        projectRepository.flush();

        var projects = projectRepository.findAllByOrderByCreatedAtDesc();

        assertThat(projects).extracting(Project::getName).containsExactly("Newer", "Older");
    }

    @Test
    void should_return_true_when_name_exists_ignore_case() {
        var project = new Project(
                null,
                "Atlas Migration",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-07-06T12:00:00Z"));
        projectRepository.saveAndFlush(project);

        assertThat(projectRepository.existsByNameIgnoreCase("atlas migration")).isTrue();
    }

    @Test
    void should_populate_created_at_when_entity_is_saved() {
        var project = new Project(null, "Audited", "Jane Doe", ProjectStatus.ACTIVE, null);

        Project savedProject = projectRepository.saveAndFlush(project);

        assertThat(savedProject.getCreatedAt()).isNotNull();
    }

    @Test
    void should_count_projects_by_status() {
        projectRepository.save(new Project(
                null,
                "Project A",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-07-06T12:00:00Z")));
        projectRepository.save(new Project(
                null,
                "Project B",
                "John Smith",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-07-06T11:00:00Z")));
        projectRepository.saveAndFlush(new Project(
                null,
                "Project C",
                "John Smith",
                ProjectStatus.BLOCKED,
                Instant.parse("2026-07-06T10:00:00Z")));

        assertThat(projectRepository.countByStatus(ProjectStatus.ACTIVE)).isEqualTo(2L);
        assertThat(projectRepository.countByStatus(ProjectStatus.BLOCKED)).isEqualTo(1L);
    }

    @Test
    void should_enforce_unique_name_constraint() {
        var first = new Project(
                null,
                "Atlas Migration",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-07-06T12:00:00Z"));
        var second = new Project(
                null,
                "Atlas Migration",
                "John Smith",
                ProjectStatus.BLOCKED,
                Instant.parse("2026-07-06T11:00:00Z"));

        projectRepository.saveAndFlush(first);

        assertThatThrownBy(() -> projectRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
