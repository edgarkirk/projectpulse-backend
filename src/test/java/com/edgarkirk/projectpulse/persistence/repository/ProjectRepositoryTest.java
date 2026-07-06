package com.edgarkirk.projectpulse.persistence.repository;

import com.edgarkirk.projectpulse.domain.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@ActiveProfiles("test")
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void should_find_all_by_order_by_created_at_desc() {
        var older = projectRepository.save(new Project(null, "Older Project", "John Doe", ProjectStatus.BLOCKED, OffsetDateTime.parse("2026-01-01T10:15:30Z")));
        var newest = projectRepository.save(new Project(null, "Newest Project", "Jane Doe", ProjectStatus.ACTIVE, OffsetDateTime.parse("2026-01-02T10:15:30Z")));

        var result = projectRepository.findAllByOrderByCreatedAtDesc();

        assertThat(result).containsExactly(newest, older);
    }

    @Test
    void should_return_true_when_name_exists_case_insensitively() {
        projectRepository.save(new Project(null, "Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, OffsetDateTime.parse("2026-01-01T10:15:30Z")));

        assertThat(projectRepository.existsByNameIgnoreCase("atlas migration")).isTrue();
    }

    @Test
    void should_return_count_by_status() {
        projectRepository.save(new Project(null, "Active Project", "Jane Doe", ProjectStatus.ACTIVE, OffsetDateTime.parse("2026-01-01T10:15:30Z")));
        projectRepository.save(new Project(null, "At Risk Project", "Jane Doe", ProjectStatus.AT_RISK, OffsetDateTime.parse("2026-01-01T10:16:30Z")));
        projectRepository.save(new Project(null, "Blocked Project", "Jane Doe", ProjectStatus.BLOCKED, OffsetDateTime.parse("2026-01-01T10:17:30Z")));

        assertThat(projectRepository.count()).isEqualTo(3L);
        assertThat(projectRepository.countByStatus(ProjectStatus.ACTIVE)).isEqualTo(1L);
        assertThat(projectRepository.countByStatus(ProjectStatus.AT_RISK)).isEqualTo(1L);
        assertThat(projectRepository.countByStatus(ProjectStatus.BLOCKED)).isEqualTo(1L);
        assertThat(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).isEqualTo(0L);
    }

    @Test
    void should_throw_constraint_violation_when_duplicate_name_is_saved() {
        projectRepository.saveAndFlush(new Project(null, "Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, OffsetDateTime.parse("2026-01-01T10:15:30Z")));

        assertThatThrownBy(() -> projectRepository.saveAndFlush(new Project(null, "atlas migration", "John Doe", ProjectStatus.BLOCKED, OffsetDateTime.parse("2026-01-01T10:16:30Z"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
