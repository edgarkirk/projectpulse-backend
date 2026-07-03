package com.edgarkirk.projectpulse.persistence.repository;

import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

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
        projectRepository.save(new Project(UUID.randomUUID(), "Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, Instant.parse("2026-07-03T10:00:00Z")));

        assertThat(projectRepository.existsByNameIgnoreCase("atlas migration")).isTrue();
    }

    @Test
    void should_returnProjectsNewestFirst_when_findingAllByOrderByCreatedAtDesc() {
        Project oldest = projectRepository.save(new Project(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Oldest", "Owner 1", ProjectStatus.ACTIVE, Instant.parse("2026-07-01T10:00:00Z")));
        Project middle = projectRepository.save(new Project(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Middle", "Owner 2", ProjectStatus.AT_RISK, Instant.parse("2026-07-02T10:00:00Z")));
        Project newest = projectRepository.save(new Project(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Newest", "Owner 3", ProjectStatus.BLOCKED, Instant.parse("2026-07-03T10:00:00Z")));

        assertThat(projectRepository.findAllByOrderByCreatedAtDesc())
                .extracting(Project::getId)
                .containsExactly(newest.getId(), middle.getId(), oldest.getId());
    }

    @Test
    void should_returnStatusCounts_when_countingByStatus() {
        projectRepository.save(new Project(UUID.randomUUID(), "Active 1", "Owner", ProjectStatus.ACTIVE, Instant.parse("2026-07-01T10:00:00Z")));
        projectRepository.save(new Project(UUID.randomUUID(), "Active 2", "Owner", ProjectStatus.ACTIVE, Instant.parse("2026-07-01T11:00:00Z")));
        projectRepository.save(new Project(UUID.randomUUID(), "Blocked 1", "Owner", ProjectStatus.BLOCKED, Instant.parse("2026-07-01T12:00:00Z")));

        assertThat(projectRepository.countByStatus(ProjectStatus.ACTIVE)).isEqualTo(2);
        assertThat(projectRepository.countByStatus(ProjectStatus.AT_RISK)).isEqualTo(0);
        assertThat(projectRepository.countByStatus(ProjectStatus.BLOCKED)).isEqualTo(1);
        assertThat(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).isEqualTo(0);
    }
}
