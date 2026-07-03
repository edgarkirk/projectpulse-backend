package com.edgarkirk.projectpulse.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edgarkirk.projectpulse.persistence.entity.Project;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void should_returnProjectsOrderedByCreatedAtDesc_when_findAllByOrderByCreatedAtDesc() {
        projectRepository.saveAll(List.of(
                new Project("Older Project", "Owner One", "Blocked", Instant.parse("2026-07-01T09:00:00Z")),
                new Project("Middle Project", "Owner Three", "At Risk", Instant.parse("2026-07-01T10:00:00Z")),
                new Project("Newest Project", "Owner Two", "Active", Instant.parse("2026-07-01T11:00:00Z"))));

        List<Project> projects = projectRepository.findAllByOrderByCreatedAtDesc();

        assertThat(projects).extracting(Project::getName)
                .containsExactly("Newest Project", "Middle Project", "Older Project");
    }

    @Test
    void should_returnTrue_when_nameMatchesIgnoringCase() {
        projectRepository.save(new Project("Atlas Migration", "Jane Doe", "Active", Instant.parse("2026-07-01T10:00:00Z")));

        assertThat(projectRepository.existsByNameIgnoreCase("atlas migration")).isTrue();
    }

    @Test
    void should_countProjectsByStatus_when_countByStatusIsCalled() {
        projectRepository.saveAll(List.of(
                new Project("Active Project", "Owner One", "Active", Instant.parse("2026-07-01T09:00:00Z")),
                new Project("At Risk Project", "Owner Two", "At Risk", Instant.parse("2026-07-01T10:00:00Z")),
                new Project("Blocked Project", "Owner Three", "Blocked", Instant.parse("2026-07-01T11:00:00Z"))));

        assertThat(projectRepository.count()).isEqualTo(3L);
        assertThat(projectRepository.countByStatus("Active")).isEqualTo(1L);
        assertThat(projectRepository.countByStatus("At Risk")).isEqualTo(1L);
        assertThat(projectRepository.countByStatus("Blocked")).isEqualTo(1L);
        assertThat(projectRepository.countByStatus("On Hold")).isEqualTo(0L);
    }
}
