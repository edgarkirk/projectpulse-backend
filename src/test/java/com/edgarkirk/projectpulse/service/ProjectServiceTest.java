package com.edgarkirk.projectpulse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private CreateProjectRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");
    }

    @Test
    void should_returnCreatedProject_when_createProjectSucceeds() {
        Project persistedProject = new Project(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Atlas Migration",
                "Jane Doe",
                "Active",
                Instant.parse("2026-07-01T10:00:00Z"));
        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(false);
        when(projectRepository.save(org.mockito.ArgumentMatchers.any(Project.class))).thenReturn(persistedProject);

        ProjectResponse response = projectService.createProject(createRequest);

        assertThat(response.id()).isEqualTo(persistedProject.getId());
        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
        assertThat(response.status()).isEqualTo("Active");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-07-01T10:00:00Z"));
    }

    @Test
    void should_throwDuplicateProjectNameException_when_nameAlreadyExists() {
        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(createRequest))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessage("Project name 'Atlas Migration' is already taken.");
    }

    @Test
    void should_returnProject_when_getProjectByIdSucceeds() {
        UUID projectId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Project project = new Project(projectId, "Atlas Migration", "Jane Doe", "Active", Instant.parse("2026-07-01T10:00:00Z"));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProjectById(projectId);

        assertThat(response.id()).isEqualTo(projectId);
        assertThat(response.name()).isEqualTo("Atlas Migration");
    }

    @Test
    void should_throwProjectNotFoundException_when_getProjectByIdFails() {
        UUID projectId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(projectId))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Project '11111111-1111-1111-1111-111111111111' was not found.");
    }

    @Test
    void should_returnProjectsOrderedByCreatedAtDesc_when_getAllProjectsIsCalled() {
        Project newest = new Project(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Newest Project", "Owner Two", "Active", Instant.parse("2026-07-01T11:00:00Z"));
        Project middle = new Project(UUID.fromString("33333333-3333-3333-3333-333333333333"), "Middle Project", "Owner Three", "At Risk", Instant.parse("2026-07-01T10:00:00Z"));
        Project older = new Project(UUID.fromString("44444444-4444-4444-4444-444444444444"), "Older Project", "Owner One", "Blocked", Instant.parse("2026-07-01T09:00:00Z"));
        when(projectRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(newest, middle, older));

        List<ProjectResponse> responses = projectService.getAllProjects();

        assertThat(responses).extracting(ProjectResponse::name)
                .containsExactly("Newest Project", "Middle Project", "Older Project");
    }

    @Test
    void should_returnDashboardSummary_when_getDashboardSummaryIsCalled() {
        when(projectRepository.count()).thenReturn(6L);
        when(projectRepository.countByStatus("Active")).thenReturn(3L);
        when(projectRepository.countByStatus("At Risk")).thenReturn(2L);
        when(projectRepository.countByStatus("Blocked")).thenReturn(1L);
        when(projectRepository.countByStatus("On Hold")).thenReturn(0L);

        DashboardSummary summary = projectService.getDashboardSummary();

        assertThat(summary.totalProjects()).isEqualTo(6);
        assertThat(summary.active()).isEqualTo(3);
        assertThat(summary.atRisk()).isEqualTo(2);
        assertThat(summary.blocked()).isEqualTo(1);
        assertThat(summary.onHold()).isEqualTo(0);
    }
}
