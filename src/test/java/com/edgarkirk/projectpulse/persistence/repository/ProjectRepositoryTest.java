package com.edgarkirk.projectpulse.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.Instant;

import com.edgarkirk.projectpulse.config.JpaAuditingConfig;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProjectRepositoryTest {

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }


    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void should_populate_created_at_when_saving_project() {
        var project = newProject();
        project.setName("Atlas Migration");
        project.setOwnerName("Jane Doe");
        project.setStatus(ProjectStatus.ACTIVE);

        var saved = projectRepository.saveAndFlush(project);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void should_return_projects_ordered_by_created_at_desc_when_finding_all_projects() {
        var oldest = saveProject("Oldest Project", "Owner One", ProjectStatus.ACTIVE);
        var middle = saveProject("Middle Project", "Owner Two", ProjectStatus.AT_RISK);
        var newest = saveProject("Newest Project", "Owner Three", ProjectStatus.BLOCKED);

        setCreatedAt(oldest.getId(), Instant.parse("2024-01-01T10:00:00Z"));
        setCreatedAt(middle.getId(), Instant.parse("2024-01-02T10:00:00Z"));
        setCreatedAt(newest.getId(), Instant.parse("2024-01-03T10:00:00Z"));
        entityManager.clear();

        var projects = projectRepository.findAllByOrderByCreatedAtDesc();

        assertThat(projects).extracting(Project::getName)
            .containsExactly("Newest Project", "Middle Project", "Oldest Project");
    }

    @Test
    void should_return_true_for_case_insensitive_name_lookup_when_project_exists() {
        saveProject("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);

        assertThat(projectRepository.existsByNameIgnoreCase("atlas migration")).isTrue();
        assertThat(projectRepository.findByNameIgnoreCase("ATLAS MIGRATION"))
            .isPresent()
            .get()
            .extracting(Project::getName)
            .isEqualTo("Atlas Migration");
    }

    @Test
    void should_count_projects_by_status_when_projects_exist() {
        saveProject("Project One", "Owner One", ProjectStatus.ACTIVE);
        saveProject("Project Two", "Owner Two", ProjectStatus.ACTIVE);
        saveProject("Project Three", "Owner Three", ProjectStatus.AT_RISK);

        assertThat(projectRepository.count()).isEqualTo(3L);
        assertThat(projectRepository.countByStatus(ProjectStatus.ACTIVE)).isEqualTo(2L);
        assertThat(projectRepository.countByStatus(ProjectStatus.AT_RISK)).isEqualTo(1L);
        assertThat(projectRepository.countByStatus(ProjectStatus.BLOCKED)).isZero();
        assertThat(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).isZero();
    }

    @Test
    void should_throw_data_integrity_violation_when_duplicate_name_is_saved() {
        saveProject("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);

        var duplicate = newProject();
        duplicate.setName("Atlas Migration");
        duplicate.setOwnerName("John Doe");
        duplicate.setStatus(ProjectStatus.BLOCKED);

        assertThatThrownBy(() -> projectRepository.saveAndFlush(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Project saveProject(String name, String ownerName, ProjectStatus status) {
        var project = newProject();
        project.setName(name);
        project.setOwnerName(ownerName);
        project.setStatus(status);
        return projectRepository.saveAndFlush(project);
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

    private void setCreatedAt(java.util.UUID id, Instant createdAt) {
        jdbcTemplate.update(
            "UPDATE projects SET created_at = ? WHERE id = ?",
            Timestamp.from(createdAt),
            id);
    }
}
