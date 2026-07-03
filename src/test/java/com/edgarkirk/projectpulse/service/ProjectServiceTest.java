package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void should_returnCreated_when_validInput() {
        given(projectRepository.findByNameIgnoreCase("Atlas Migration")).willReturn(Optional.empty());
        given(projectRepository.save(any(Project.class))).willAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = projectService.createProject(new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active"));

        assertThat(response.id()).isNotBlank();
        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
        assertThat(response.status()).isEqualTo("Active");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void should_return409_when_duplicateName() {
        given(projectRepository.findByNameIgnoreCase("Atlas Migration")).willReturn(Optional.of(new Project("Atlas Migration", "Jane Doe", "Active")));

        assertThatThrownBy(() -> projectService.createProject(new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active")))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessage("Project name 'Atlas Migration' is already taken");
    }

    @Test
    void should_returnAllItems_when_getAll() {
        Project project = new Project("Atlas Migration", "Jane Doe", "Active");
        given(projectRepository.findAllByOrderByCreatedAtDescIdDesc()).willReturn(List.of(project));

        List<ProjectResponse> responses = projectService.listProjects();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().name()).isEqualTo("Atlas Migration");
    }

    @Test
    void should_returnProject_when_projectExists() {
        UUID id = UUID.randomUUID();
        Project project = new Project("Atlas Migration", "Jane Doe", "Active");
        given(projectRepository.findById(id)).willReturn(Optional.of(project));

        ProjectResponse response = projectService.getProject(id);

        assertThat(response.id()).isEqualTo(id.toString());
        assertThat(response.name()).isEqualTo("Atlas Migration");
    }

    @Test
    void should_return404_when_itemNotFound() {
        UUID id = UUID.randomUUID();
        given(projectRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(id))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Project with id %s was not found".formatted(id));
    }

    @Test
    void should_returnDashboardSummary_when_projectsExist() {
        given(projectRepository.countByStatus("Active")).willReturn(3L);
        given(projectRepository.countByStatus("At Risk")).willReturn(2L);
        given(projectRepository.countByStatus("Blocked")).willReturn(1L);
        given(projectRepository.countByStatus("On Hold")).willReturn(0L);

        DashboardSummary summary = projectService.getDashboardSummary();

        assertThat(summary.totalProjects()).isEqualTo(6);
        assertThat(summary.active()).isEqualTo(3);
        assertThat(summary.atRisk()).isEqualTo(2);
        assertThat(summary.blocked()).isEqualTo(1);
        assertThat(summary.onHold()).isEqualTo(0);
    }
}
