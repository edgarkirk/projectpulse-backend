package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.domain.ProjectStatus;
import com.edgarkirk.projectpulse.mapper.ProjectMapper;
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
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    private final ProjectMapper projectMapper = new ProjectMapper();

    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, projectMapper);
    }

    @Test
    void should_create_project_when_name_is_unique() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        var saved = org.springframework.beans.BeanUtils.instantiateClass(Project.class);
        saved.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        saved.setName("Atlas Migration");
        saved.setOwnerName("Jane Doe");
        saved.setStatus(ProjectStatus.ACTIVE);
        saved.setCreatedAt(Instant.parse("2026-01-01T10:15:30Z"));

        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(false);
        when(projectRepository.saveAndFlush(any(Project.class))).thenReturn(saved);

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.status()).isEqualTo("Active");
    }

    @Test
    void should_throw_duplicate_exception_when_save_detects_constraint_violation() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(false);
        when(projectRepository.saveAndFlush(any(Project.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessage("project name already exists: Atlas Migration");
    }

    @Test
    void should_return_project_when_id_exists() {
        var project = org.springframework.beans.BeanUtils.instantiateClass(Project.class);
        project.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        project.setName("Atlas Migration");
        project.setOwnerName("Jane Doe");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedAt(Instant.parse("2026-01-01T10:15:30Z"));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProject(project.getId());

        assertThat(response.name()).isEqualTo("Atlas Migration");
    }

    @Test
    void should_throw_not_found_when_project_missing() {
        var id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(id))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("project not found for id 11111111-1111-1111-1111-111111111111");
    }

    @Test
    void should_return_projects_ordered_newest_first_when_listing() {
        var older = org.springframework.beans.BeanUtils.instantiateClass(Project.class);
        older.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        older.setName("Older Project");
        older.setOwnerName("Jane Doe");
        older.setStatus(ProjectStatus.BLOCKED);
        older.setCreatedAt(Instant.parse("2026-01-01T10:15:30Z"));

        var newer = org.springframework.beans.BeanUtils.instantiateClass(Project.class);
        newer.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        newer.setName("Newer Project");
        newer.setOwnerName("Jane Doe");
        newer.setStatus(ProjectStatus.ACTIVE);
        newer.setCreatedAt(Instant.parse("2026-01-02T10:15:30Z"));

        when(projectRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(newer, older));

        var responses = projectService.listProjects();

        assertThat(responses).extracting(ProjectResponse::name).containsExactly("Newer Project", "Older Project");
    }

    @Test
    void should_return_summary_counts_when_aggregating() {
        when(projectRepository.count()).thenReturn(6L);
        when(projectRepository.countByStatus(ProjectStatus.ACTIVE)).thenReturn(3L);
        when(projectRepository.countByStatus(ProjectStatus.AT_RISK)).thenReturn(2L);
        when(projectRepository.countByStatus(ProjectStatus.BLOCKED)).thenReturn(1L);
        when(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).thenReturn(0L);

        DashboardSummary summary = projectService.getDashboardSummary();

        assertThat(summary).isEqualTo(new DashboardSummary(6L, 3L, 2L, 1L, 0L));
    }
}
