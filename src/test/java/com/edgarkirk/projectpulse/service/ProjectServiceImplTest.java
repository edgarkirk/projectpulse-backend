package com.edgarkirk.projectpulse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.mapper.ProjectMapper;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Spy
    private ProjectMapper projectMapper = new ProjectMapper();

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void should_create_project_when_request_is_valid() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        var savedProject = new Project(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Atlas Migration",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-07-06T12:00:00Z"));

        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(false);
        when(projectRepository.saveAndFlush(any(Project.class))).thenReturn(savedProject);

        ProjectResponse response = projectService.create(request);

        assertThat(response.id()).isEqualTo(savedProject.getId());
        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
        assertThat(response.status()).isEqualTo(ProjectStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(savedProject.getCreatedAt());
        verify(projectRepository).existsByNameIgnoreCase("Atlas Migration");
    }

    @Test
    void should_trim_project_fields_when_request_contains_surrounding_whitespace() {
        var request = new CreateProjectRequest("  Atlas Migration  ", "  Jane Doe  ", ProjectStatus.ACTIVE);
        var savedProject = new Project(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Atlas Migration",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-07-06T12:00:00Z"));

        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(false);
        when(projectRepository.saveAndFlush(any(Project.class))).thenReturn(savedProject);

        ProjectResponse response = projectService.create(request);

        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
    }

    @Test
    void should_return_not_found_when_project_is_absent() {
        var projectId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getById(projectId))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Project not found");
    }

    @Test
    void should_return_duplicate_error_when_name_is_taken() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(true);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessage("Project name already taken");

        verify(projectRepository, never()).saveAndFlush(any());
    }

    @Test
    void should_return_duplicate_error_when_save_fails_with_unique_constraint() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(false);
        when(projectRepository.saveAndFlush(any(Project.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessage("Project name already taken");
    }

    @Test
    void should_return_projects_in_descending_created_at_order() {
        var first = new Project(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Newest",
                "Jane Doe",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-07-06T12:00:00Z"));
        var second = new Project(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Older",
                "John Smith",
                ProjectStatus.BLOCKED,
                Instant.parse("2026-07-05T12:00:00Z"));
        when(projectRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(first, second));

        List<ProjectResponse> responses = projectService.listAll();

        assertThat(responses).extracting(ProjectResponse::name).containsExactly("Newest", "Older");
    }

    @Test
    void should_compute_dashboard_summary_from_current_counts() {
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
}
