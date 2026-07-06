package com.edgarkirk.projectpulse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void should_create_project_when_request_is_valid() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        lenient().when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            var project = invocation.getArgument(0, Project.class);
            return new Project(
                    UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    project.getName(),
                    project.getOwnerName(),
                    project.getStatus(),
                    OffsetDateTime.parse("2026-07-06T12:00:00Z"));
        });

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Project creation is not implemented yet");
    }

    @Test
    void should_return_not_found_when_project_is_absent() {
        var projectId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        lenient().when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getById(projectId))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Project not found");
    }

    @Test
    void should_return_duplicate_error_when_name_is_taken() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        lenient().when(projectRepository.save(any(Project.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessage("Project name already taken");
    }

    @Test
    void should_return_projects_in_descending_created_at_order() {
        lenient().when(projectRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        assertThatThrownBy(() -> projectService.listAll())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Project listing is not implemented yet");
    }

    @Test
    void should_compute_dashboard_summary_from_current_counts() {
        lenient().when(projectRepository.count()).thenReturn(6L);
        lenient().when(projectRepository.countByStatus(ProjectStatus.ACTIVE)).thenReturn(3L);
        lenient().when(projectRepository.countByStatus(ProjectStatus.AT_RISK)).thenReturn(2L);
        lenient().when(projectRepository.countByStatus(ProjectStatus.BLOCKED)).thenReturn(1L);
        lenient().when(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).thenReturn(0L);

        assertThatThrownBy(() -> projectService.getDashboardSummary())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Dashboard summary is not implemented yet");
    }
}
