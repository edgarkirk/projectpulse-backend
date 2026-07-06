package com.edgarkirk.projectpulse.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.edgarkirk.projectpulse.persistence.entity.TestProjectEntity;
import com.edgarkirk.projectpulse.persistence.repository.TestProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProjectRepositoryTest {

    @Autowired
    private TestProjectRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void should_find_all_by_order_by_created_at_desc_when_projects_exist() {
        repository.save(new TestProjectEntity(
                UUID.randomUUID(),
                "First",
                "Jane Doe",
                "Active",
                OffsetDateTime.of(2026, 7, 1, 8, 0, 0, 0, ZoneOffset.UTC)));
        repository.save(new TestProjectEntity(
                UUID.randomUUID(),
                "Second",
                "John Doe",
                "Blocked",
                OffsetDateTime.of(2026, 7, 2, 8, 0, 0, 0, ZoneOffset.UTC)));

        var results = repository.findAllByOrderByCreatedAtDesc();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("Second");
        assertThat(results.get(1).getName()).isEqualTo("First");
    }

    @Test
    void should_return_true_when_name_exists_case_insensitively() {
        repository.save(new TestProjectEntity(
                UUID.randomUUID(),
                "Atlas Migration",
                "Jane Doe",
                "Active",
                OffsetDateTime.now(ZoneOffset.UTC)));

        assertThat(repository.existsByNameIgnoreCase("atlas migration")).isTrue();
    }

    @Test
    void should_count_projects_by_status_when_multiple_statuses_exist() {
        repository.save(new TestProjectEntity(
                UUID.randomUUID(),
                "Active Project",
                "Jane Doe",
                "Active",
                OffsetDateTime.now(ZoneOffset.UTC)));
        repository.save(new TestProjectEntity(
                UUID.randomUUID(),
                "At Risk Project",
                "Jane Doe",
                "At Risk",
                OffsetDateTime.now(ZoneOffset.UTC)));
        repository.save(new TestProjectEntity(
                UUID.randomUUID(),
                "Blocked Project",
                "Jane Doe",
                "Blocked",
                OffsetDateTime.now(ZoneOffset.UTC)));

        assertThat(repository.count()).isEqualTo(3);
        assertThat(repository.countByStatus("Active")).isEqualTo(1);
        assertThat(repository.countByStatus("At Risk")).isEqualTo(1);
        assertThat(repository.countByStatus("Blocked")).isEqualTo(1);
    }
}
