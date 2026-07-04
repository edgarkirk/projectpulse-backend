package com.edgarkirk.projectpulse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.mapper.ProjectMapper;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void set_up() {
        projectService = new ProjectServiceImpl(projectRepository, projectMapper);
    }

    @Test
    void should_create_project_and_trim_text_fields_when_request_contains_surrounding_whitespace() {
        CreateProjectRequest request = new CreateProjectRequest("  Atlas Migration  ", "  Jane Doe  ", "Active");
        Project persistedProject = new Project("Atlas Migration", "Jane Doe", "Active");
        setField(persistedProject, "id", UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        setField(persistedProject, "createdAt", Instant.parse("2026-07-03T09:20:00Z"));

        when(projectRepository.findByNameIgnoreCase("Atlas Migration")).thenReturn(Optional.empty());
        when(projectRepository.save(any())).thenReturn(persistedProject);
        when(projectMapper.toEntity(any())).thenAnswer(invocation -> {
            CreateProjectRequest normalized = invocation.getArgument(0);
            return new Project(normalized.name(), normalized.ownerName(), normalized.status());
        });
        when(projectMapper.toResponse(persistedProject)).thenReturn(new ProjectResponse(
                persistedProject.getId(), persistedProject.getName(), persistedProject.getOwnerName(), persistedProject.getStatus(), persistedProject.getCreatedAt()));

        ProjectResponse response = projectService.createProject(request);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        assertThat(projectCaptor.getValue().getName()).isEqualTo("Atlas Migration");
        assertThat(projectCaptor.getValue().getOwnerName()).isEqualTo("Jane Doe");
        assertThat(response.name()).isEqualTo("Atlas Migration");
        assertThat(response.ownerName()).isEqualTo("Jane Doe");
        assertThat(response.status()).isEqualTo("Active");
    }

    @Test
    void should_reject_duplicate_project_name_when_name_matches_existing_project_case_insensitively() {
        when(projectRepository.findByNameIgnoreCase("Atlas Migration")).thenReturn(Optional.of(new Project("Atlas Migration", "Jane Doe", "Active")));

        assertThatThrownBy(() -> projectService.createProject(new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active")))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessage("Project name is already taken");

        verifyNoInteractions(projectMapper);
    }

    @Test
    void should_return_projects_ordered_newest_first_when_listing_projects() {
        Project newest = new Project("Newest", "Owner Two", "Blocked");
        setField(newest, "createdAt", Instant.parse("2026-07-03T10:00:00Z"));
        Project older = new Project("Older", "Owner One", "Active");
        setField(older, "createdAt", Instant.parse("2026-07-03T09:00:00Z"));
        when(projectRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(newest, older));
        when(projectMapper.toResponses(List.of(newest, older))).thenReturn(List.of(
                new ProjectResponse(UUID.randomUUID(), "Newest", "Owner Two", "Blocked", newest.getCreatedAt()),
                new ProjectResponse(UUID.randomUUID(), "Older", "Owner One", "Active", older.getCreatedAt())));

        List<ProjectResponse> responses = projectService.listProjects();

        assertThat(responses).extracting(ProjectResponse::name).containsExactly("Newest", "Older");
    }

    @Test
    void should_return_project_when_identifier_exists() {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Project project = new Project("Atlas Migration", "Jane Doe", "Active");
        setField(project, "id", id);
        setField(project, "createdAt", Instant.parse("2026-07-03T09:20:00Z"));
        when(projectRepository.findById(id)).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(project)).thenReturn(new ProjectResponse(id, project.getName(), project.getOwnerName(), project.getStatus(), project.getCreatedAt()));

        ProjectResponse response = projectService.getProjectById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Atlas Migration");
    }

    @Test
    void should_throw_not_found_when_identifier_is_unknown() {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(id))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("Project with id aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa was not found");

        verify(projectRepository).findById(id);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void should_compute_dashboard_summary_from_current_database_state_when_requested() {
        when(projectRepository.count()).thenReturn(6L);
        when(projectRepository.countByStatus("Active")).thenReturn(3L);
        when(projectRepository.countByStatus("At Risk")).thenReturn(2L);
        when(projectRepository.countByStatus("Blocked")).thenReturn(1L);
        when(projectRepository.countByStatus("On Hold")).thenReturn(0L);

        DashboardSummary summary = projectService.getDashboardSummary();

        assertThat(summary).isEqualTo(new DashboardSummary(6, 3, 2, 1, 0));
    }

    private static void setField(Project project, String fieldName, Object value) {
        try {
            var field = Project.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(project, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
