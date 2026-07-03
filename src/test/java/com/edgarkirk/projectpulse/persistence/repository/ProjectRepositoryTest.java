package com.edgarkirk.projectpulse.persistence.repository;

import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }

    @Test
    void should_returnTrue_when_nameExistsIgnoringCase() {
        persistProject("Atlas Migration", ProjectStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));

        assertThat(projectRepository.existsByNameIgnoreCase("atlas migration")).isTrue();
    }

    @Test
    void should_returnProjectsOrderedNewestFirst_when_findAllByOrderByCreatedAtDescIdDesc() {
        Project oldest = persistProject("Alpha", ProjectStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC).minusDays(3));
        Project middle = persistProject("Beta", ProjectStatus.BLOCKED, OffsetDateTime.now(ZoneOffset.UTC).minusDays(2));
        Project newest = persistProject("Gamma", ProjectStatus.AT_RISK, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));

        List<Project> projects = projectRepository.findAllByOrderByCreatedAtDescIdDesc();

        assertThat(projects).extracting(Project::getId).containsExactly(newest.getId(), middle.getId(), oldest.getId());
    }

    @Test
    void should_countProjectsPerStatus_when_countingDashboardSummary() {
        persistProject("Alpha", ProjectStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC).minusDays(4));
        persistProject("Beta", ProjectStatus.ACTIVE, OffsetDateTime.now(ZoneOffset.UTC).minusDays(3));
        persistProject("Gamma", ProjectStatus.AT_RISK, OffsetDateTime.now(ZoneOffset.UTC).minusDays(2));
        persistProject("Delta", ProjectStatus.BLOCKED, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        persistProject("Epsilon", ProjectStatus.ON_HOLD, OffsetDateTime.now(ZoneOffset.UTC));

        assertThat(projectRepository.count()).isEqualTo(5);
        assertThat(projectRepository.countByStatus(ProjectStatus.ACTIVE)).isEqualTo(2);
        assertThat(projectRepository.countByStatus(ProjectStatus.AT_RISK)).isEqualTo(1);
        assertThat(projectRepository.countByStatus(ProjectStatus.BLOCKED)).isEqualTo(1);
        assertThat(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).isEqualTo(1);
    }

    private Project persistProject(String name, ProjectStatus status, OffsetDateTime createdAt) {
        Project project = new Project();
        project.setName(name);
        project.setOwnerName(name + " Owner");
        project.setStatus(status);
        project.setCreatedAt(createdAt);
        return projectRepository.saveAndFlush(project);
    }
}
