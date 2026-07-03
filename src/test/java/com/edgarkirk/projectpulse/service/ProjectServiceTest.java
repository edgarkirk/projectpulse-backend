package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void should_returnCreatedProject_when_validInput() {
        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(false);

        ProjectResponse response = projectService.createProject(new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active"));

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
        assertThat(response.status()).isEqualTo("Active");
        assertThat(response.createdAt()).isNotNull();
        verify(projectRepository).existsByNameIgnoreCase("Atlas Migration");
    }

    @Test
    void should_throwDuplicateProjectNameException_when_nameAlreadyExists() {
        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active")))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void should_throwProjectNotFoundException_when_projectMissing() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> projectService.getProjectById(id))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void should_returnDashboardSummary_when_projectsExist() {
        DashboardSummary summary = projectService.getDashboardSummary();

        assertThat(summary.totalProjects()).isEqualTo(6);
        assertThat(summary.active()).isEqualTo(3);
        assertThat(summary.atRisk()).isEqualTo(2);
        assertThat(summary.blocked()).isEqualTo(1);
        assertThat(summary.onHold()).isEqualTo(0);
    }

    @Test
    void should_returnOrderedProjects_when_listingProjects() {
        List<ProjectResponse> response = projectService.listProjects();

        assertThat(response).hasSize(3);
        assertThat(response).extracting(ProjectResponse::name).containsExactly("Gamma", "Beta", "Alpha");
    }
}
