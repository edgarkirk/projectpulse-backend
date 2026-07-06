package com.edgarkirk.projectpulse.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.TestCreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.request.TestProjectStatus;
import com.edgarkirk.projectpulse.api.dto.response.TestDashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.TestProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.TestProjectEntity;
import com.edgarkirk.projectpulse.persistence.repository.TestProjectRepository;
import com.edgarkirk.projectpulse.service.exception.TestDuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.TestProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private TestProjectRepository repository;

    @InjectMocks
    private TestProjectServiceImpl service;

    @Test
    void should_return_created_project_when_request_is_valid() {
        var saved = new TestProjectEntity(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Atlas Migration",
                "Jane Doe",
                "Active",
                OffsetDateTime.of(2026, 7, 6, 10, 0, 0, 0, ZoneOffset.UTC));
        when(repository.save(any(TestProjectEntity.class))).thenReturn(saved);

        TestProjectResponse response = service.create(new TestCreateProjectRequest(
                "Atlas Migration",
                "Jane Doe",
                TestProjectStatus.ACTIVE));

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
        assertThat(response.status()).isEqualTo(TestProjectStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    void should_throw_duplicate_project_name_exception_when_repository_rejects_duplicate() {
        when(repository.save(any(TestProjectEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> service.create(new TestCreateProjectRequest(
                "Atlas Migration",
                "Jane Doe",
                TestProjectStatus.ACTIVE)))
                .isInstanceOf(TestDuplicateProjectNameException.class)
                .hasMessage("Project name Atlas Migration is already taken");
    }

    @Test
    void should_throw_project_not_found_exception_when_id_does_not_exist() {
        when(repository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(UUID.fromString("22222222-2222-2222-2222-222222222222")))
                .isInstanceOf(TestProjectNotFoundException.class)
                .hasMessage("Project not found");
    }

    @Test
    void should_return_dashboard_summary_when_database_contains_projects() {
        when(repository.count()).thenReturn(6L);
        when(repository.countByStatus("Active")).thenReturn(3L);
        when(repository.countByStatus("At Risk")).thenReturn(2L);
        when(repository.countByStatus("Blocked")).thenReturn(1L);
        when(repository.countByStatus("On Hold")).thenReturn(0L);

        TestDashboardSummary summary = service.getDashboardSummary();

        assertThat(summary.totalProjects()).isEqualTo(6);
        assertThat(summary.active()).isEqualTo(3);
        assertThat(summary.atRisk()).isEqualTo(2);
        assertThat(summary.blocked()).isEqualTo(1);
        assertThat(summary.onHold()).isEqualTo(0);
    }

    @Test
    void should_return_projects_ordered_by_created_at_desc_when_listing_projects() {
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(
                new TestProjectEntity(
                        UUID.fromString("33333333-3333-3333-3333-333333333333"),
                        "Newest",
                        "Jane Doe",
                        "Active",
                        OffsetDateTime.of(2026, 7, 6, 11, 0, 0, 0, ZoneOffset.UTC)),
                new TestProjectEntity(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        "Older",
                        "Jane Doe",
                        "Blocked",
                        OffsetDateTime.of(2026, 7, 6, 9, 0, 0, 0, ZoneOffset.UTC))));

        var results = service.findAll();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("Newest");
        assertThat(results.get(1).name()).isEqualTo("Older");
    }
}
