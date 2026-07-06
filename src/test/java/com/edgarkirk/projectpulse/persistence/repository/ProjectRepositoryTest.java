package com.edgarkirk.projectpulse.persistence.repository;

import com.edgarkirk.projectpulse.config.JpaAuditingConfig;
import com.edgarkirk.projectpulse.domain.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void should_persist_project_with_auditing_when_saved() {
        var project = org.springframework.beans.BeanUtils.instantiateClass(Project.class);
        project.setName("Atlas Migration");
        project.setOwnerName("Jane Doe");
        project.setStatus(ProjectStatus.ACTIVE);

        Project saved = projectRepository.saveAndFlush(project);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void should_find_projects_ordered_by_created_at_desc_when_requested() {
        var older = org.springframework.beans.BeanUtils.instantiateClass(Project.class);
        older.setName("Older Project");
        older.setOwnerName("Jane Doe");
        older.setStatus(ProjectStatus.BLOCKED);
        older.setCreatedAt(Instant.parse("2026-01-01T10:15:30Z"));

        var newer = org.springframework.beans.BeanUtils.instantiateClass(Project.class);
        newer.setName("Newer Project");
        newer.setOwnerName("Jane Doe");
        newer.setStatus(ProjectStatus.ACTIVE);
        newer.setCreatedAt(Instant.parse("2026-01-02T10:15:30Z"));

        projectRepository.saveAndFlush(older);
        projectRepository.saveAndFlush(newer);

        var projects = projectRepository.findAllByOrderByCreatedAtDesc();

        assertThat(projects).extracting(Project::getName).containsExactly("Newer Project", "Older Project");
    }

    @Test
    void should_count_projects_by_status_when_requested() {
        saveProject("Active One", ProjectStatus.ACTIVE);
        saveProject("Active Two", ProjectStatus.ACTIVE);
        saveProject("At Risk One", ProjectStatus.AT_RISK);
        saveProject("Blocked One", ProjectStatus.BLOCKED);

        assertThat(projectRepository.count()).isEqualTo(4L);
        assertThat(projectRepository.countByStatus(ProjectStatus.ACTIVE)).isEqualTo(2L);
        assertThat(projectRepository.countByStatus(ProjectStatus.AT_RISK)).isEqualTo(1L);
        assertThat(projectRepository.countByStatus(ProjectStatus.BLOCKED)).isEqualTo(1L);
        assertThat(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).isEqualTo(0L);
    }

    @Test
    void should_fail_when_duplicate_name_is_saved_exactly() {
        saveProject("Atlas Migration", ProjectStatus.ACTIVE);

        assertThatThrownBy(() -> saveProject("Atlas Migration", ProjectStatus.BLOCKED))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_support_case_insensitive_name_lookup() {
        saveProject("Atlas Migration", ProjectStatus.ACTIVE);

        assertThat(projectRepository.existsByNameIgnoreCase("atlas migration")).isTrue();
        assertThat(projectRepository.findByNameIgnoreCase("ATLAS MIGRATION")).isPresent();
    }

    private Project saveProject(String name, ProjectStatus status) {
        var project = org.springframework.beans.BeanUtils.instantiateClass(Project.class);
        project.setName(name);
        project.setOwnerName("Jane Doe");
        project.setStatus(status);
        return projectRepository.saveAndFlush(project);
    }
}
