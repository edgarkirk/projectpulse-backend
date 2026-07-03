package com.edgarkirk.projectpulse.persistence.repository;

import com.edgarkirk.projectpulse.persistence.entity.Project;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void should_returnProjectsNewestFirst_when_listingProjects() {
        insertProject(UUID.randomUUID(), "Old Project", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-01T10:00:00Z"));
        insertProject(UUID.randomUUID(), "New Project", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-03T10:00:00Z"));
        insertProject(UUID.randomUUID(), "Middle Project", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-02T10:00:00Z"));

        List<Project> projects = projectRepository.findAllByOrderByCreatedAtDescIdDesc();

        assertThat(projects).extracting(Project::getName).containsExactly("New Project", "Middle Project", "Old Project");
    }

    @Test
    void should_countProjectsByStatus_when_statusBucketsExist() {
        insertProject(UUID.randomUUID(), "A", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-01T10:00:00Z"));
        insertProject(UUID.randomUUID(), "B", "Jane Doe", "Active", OffsetDateTime.parse("2026-07-02T10:00:00Z"));
        insertProject(UUID.randomUUID(), "C", "Jane Doe", "Blocked", OffsetDateTime.parse("2026-07-03T10:00:00Z"));

        assertThat(projectRepository.countByStatus("Active")).isEqualTo(2L);
        assertThat(projectRepository.countByStatus("Blocked")).isEqualTo(1L);
    }

    @Test
    void should_rejectDuplicateNameIgnoringCase_when_savingProjects() {
        projectRepository.saveAndFlush(new Project("Atlas Migration", "Jane Doe", "Active"));

        assertThatThrownBy(() -> projectRepository.saveAndFlush(new Project("atlas migration", "John Smith", "At Risk")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertProject(UUID id, String name, String ownerName, String status, OffsetDateTime createdAt) {
        jdbcTemplate.update("insert into projects (id, name, owner_name, status, created_at) values (?, ?, ?, ?, ?)",
                id,
                name,
                ownerName,
                status,
                Timestamp.from(createdAt.toInstant()));
    }
}
