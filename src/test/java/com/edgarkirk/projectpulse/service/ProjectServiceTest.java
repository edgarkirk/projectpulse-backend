package com.edgarkirk.projectpulse.service;

import java.lang.reflect.Constructor;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.request.ProjectStatus;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.mapper.ProjectMapper;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Spy
    private ProjectMapper projectMapper = new ProjectMapper();

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void should_return_created_project_when_request_is_valid() {
        var saved = project("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE,
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                OffsetDateTime.of(2026, 7, 6, 10, 0, 0, 0, ZoneOffset.UTC));
        when(projectRepository.saveAndFlush(any(Project.class))).thenReturn(saved);

        ProjectResponse response = projectService.create(new CreateProjectRequest(
                "Atlas Migration",
                "Jane Doe",
                ProjectStatus.ACTIVE));

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
        assertThat(response.status()).isEqualTo("Active");
        assertThat(response.createdAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    void should_throw_duplicate_project_name_exception_when_repository_rejects_duplicate() {
        when(projectRepository.saveAndFlush(any(Project.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> projectService.create(new CreateProjectRequest(
                "Atlas Migration",
                "Jane Doe",
                ProjectStatus.ACTIVE)))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessage("Project name 'Atlas Migration' already exists");
    }

    @Test
    void should_throw_project_not_found_exception_when_id_does_not_exist() {
        var id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(id))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Project with id 22222222-2222-2222-2222-222222222222 not found");
    }

    @Test
    void should_return_dashboard_summary_when_database_contains_projects() {
        when(projectRepository.count()).thenReturn(6L);
        when(projectRepository.countByStatus(ProjectStatus.ACTIVE)).thenReturn(3L);
        when(projectRepository.countByStatus(ProjectStatus.AT_RISK)).thenReturn(2L);
        when(projectRepository.countByStatus(ProjectStatus.BLOCKED)).thenReturn(1L);
        when(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).thenReturn(0L);

        DashboardSummary summary = projectService.getDashboardSummary();

        assertThat(summary.totalProjects()).isEqualTo(6);
        assertThat(summary.active()).isEqualTo(3);
        assertThat(summary.atRisk()).isEqualTo(2);
        assertThat(summary.blocked()).isEqualTo(1);
        assertThat(summary.onHold()).isEqualTo(0);
    }

    @Test
    void should_return_projects_ordered_by_created_at_desc_when_listing_projects() {
        when(projectRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(
                project("Newest", "Jane Doe", ProjectStatus.ACTIVE,
                        UUID.fromString("33333333-3333-3333-3333-333333333333"),
                        OffsetDateTime.of(2026, 7, 6, 11, 0, 0, 0, ZoneOffset.UTC)),
                project("Older", "Jane Doe", ProjectStatus.BLOCKED,
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        OffsetDateTime.of(2026, 7, 6, 9, 0, 0, 0, ZoneOffset.UTC))));

        var results = projectService.findAll();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("Newest");
        assertThat(results.get(1).name()).isEqualTo("Older");
    }

    private Project project(String name, String ownerName, ProjectStatus status, UUID id, OffsetDateTime createdAt) {
        try {
            Constructor<Project> constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Project project = constructor.newInstance();
            project.setId(id);
            project.setName(name);
            project.setOwnerName(ownerName);
            project.setStatus(status);
            project.setCreatedAt(createdAt);
            return project;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create project", e);
        }
    }
}
