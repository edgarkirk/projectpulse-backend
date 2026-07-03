package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummaryResponse;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void should_returnCreatedProject_when_nameIsAvailable() {
        CreateProjectRequest request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");
        Project savedProject = new Project(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, Instant.parse("2026-07-03T10:00:00Z"));

        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(false);
        when(projectRepository.save(org.mockito.ArgumentMatchers.any(Project.class))).thenReturn(savedProject);

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.id()).isEqualTo(savedProject.getId());
        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
        assertThat(response.status()).isEqualTo("Active");
        assertThat(response.createdAt()).isEqualTo(savedProject.getCreatedAt());
    }

    @Test
    void should_throwDuplicateProjectNameException_when_nameAlreadyExists() {
        CreateProjectRequest request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");
        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessageContaining("Atlas Migration");
    }

    @Test
    void should_returnProjectsNewestFirst_when_listingProjects() {
        Project newest = new Project(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Newest", "Owner 3", ProjectStatus.BLOCKED, Instant.parse("2026-07-03T10:00:00Z"));
        Project older = new Project(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Older", "Owner 2", ProjectStatus.AT_RISK, Instant.parse("2026-07-02T10:00:00Z"));
        when(projectRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(newest, older));

        List<ProjectResponse> responses = projectService.listProjects();

        assertThat(responses).extracting(ProjectResponse::id).containsExactly(newest.getId(), older.getId());
    }

    @Test
    void should_throwProjectNotFoundException_when_projectDoesNotExist() {
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(id))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void should_returnDashboardSummary_when_countsAreRequested() {
        when(projectRepository.count()).thenReturn(6L);
        when(projectRepository.countByStatus(ProjectStatus.ACTIVE)).thenReturn(3L);
        when(projectRepository.countByStatus(ProjectStatus.AT_RISK)).thenReturn(2L);
        when(projectRepository.countByStatus(ProjectStatus.BLOCKED)).thenReturn(1L);
        when(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).thenReturn(0L);

        DashboardSummaryResponse response = projectService.getDashboardSummary();

        assertThat(response.totalProjects()).isEqualTo(6L);
        assertThat(response.active()).isEqualTo(3L);
        assertThat(response.atRisk()).isEqualTo(2L);
        assertThat(response.blocked()).isEqualTo(1L);
        assertThat(response.onHold()).isEqualTo(0L);
    }
}
