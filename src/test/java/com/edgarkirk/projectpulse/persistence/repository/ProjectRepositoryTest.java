package com.edgarkirk.projectpulse.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.edgarkirk.projectpulse.ProjectPulseApplication;

import com.edgarkirk.projectpulse.persistence.entity.Project;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = ProjectPulseApplication.class)
@Import(ProjectRepositoryTest.TestJpaConfig.class)
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void should_persist_project_with_generated_id_and_created_at_when_saving() {
        Project project = new Project("Atlas Migration", "Jane Doe", "Active");

        Project savedProject = projectRepository.saveAndFlush(project);

        assertThat(savedProject.getId()).isNotNull();
        assertThat(savedProject.getCreatedAt()).isNotNull();
    }

    @Test
    void should_return_projects_ordered_by_created_at_desc_when_listing_all() {
        Project older = projectRepository.saveAndFlush(new Project("First", "Alice", "Active"));
        entityManager.detach(older);
        Project newer = projectRepository.saveAndFlush(new Project("Second", "Bob", "Blocked"));
        entityManager.detach(newer);

        List<Project> projects = projectRepository.findAllByOrderByCreatedAtDesc();

        assertThat(projects).extracting(Project::getName).containsExactly("Second", "First");
    }

    @Test
    void should_find_project_by_name_case_insensitively_when_lookup_uses_ignore_case() {
        Project project = projectRepository.saveAndFlush(new Project("Atlas Migration", "Jane Doe", "Active"));
        entityManager.detach(project);

        assertThat(projectRepository.findByNameIgnoreCase("atlas migration")).isPresent();
        assertThat(projectRepository.existsByNameIgnoreCase("ATLAS MIGRATION")).isTrue();
    }

    @Test
    void should_reject_project_names_that_differ_only_by_case_when_saving() {
        projectRepository.saveAndFlush(new Project("Atlas Migration", "Jane Doe", "Active"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                projectRepository.saveAndFlush(new Project("atlas migration", "John Doe", "Blocked")))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }


    @Test
    void should_count_projects_by_status_when_aggregating_dashboard_data() {
        projectRepository.saveAndFlush(new Project("A", "Owner A", "Active"));
        projectRepository.saveAndFlush(new Project("B", "Owner B", "Active"));
        projectRepository.saveAndFlush(new Project("C", "Owner C", "Blocked"));

        assertThat(projectRepository.countByStatus("Active")).isEqualTo(2L);
        assertThat(projectRepository.countByStatus("Blocked")).isEqualTo(1L);
        assertThat(projectRepository.count()).isEqualTo(3L);
    }

    @Configuration
    @EnableJpaAuditing
    static class TestJpaConfig {
    }
}
