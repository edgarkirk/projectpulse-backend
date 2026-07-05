package com.edgarkirk.projectpulse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.mapper.ProjectMapper;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void should_create_project_when_request_is_valid() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");
        var entity = newProject("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        var savedEntity = newProject("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        savedEntity.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        savedEntity.setCreatedAt(Instant.parse("2024-01-01T10:15:30Z"));
        var response = new ProjectResponse(
            savedEntity.getId(),
            savedEntity.getName(),
            savedEntity.getOwnerName(),
            savedEntity.getStatus().getDisplayName(),
            savedEntity.getCreatedAt());

        when(projectRepository.existsByNameIgnoreCase(request.name())).thenReturn(false);
        when(projectMapper.toEntity(request)).thenReturn(entity);
        when(projectRepository.save(entity)).thenReturn(savedEntity);
        when(projectMapper.toResponse(savedEntity)).thenReturn(response);

        var result = projectService.create(request);

        assertThat(result).isEqualTo(response);
        verify(projectRepository).existsByNameIgnoreCase(request.name());
        verify(projectMapper).toEntity(request);
        verify(projectRepository).save(entity);
        verify(projectMapper).toResponse(savedEntity);
        verifyNoMoreInteractions(projectRepository, projectMapper);
    }

    @Test
    void should_throw_duplicate_project_exception_when_project_name_already_exists() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");

        when(projectRepository.existsByNameIgnoreCase(request.name())).thenReturn(true);

        assertThatThrownBy(() -> projectService.create(request))
            .isInstanceOf(DuplicateProjectException.class)
            .hasMessage("Project name Atlas Migration is already taken");

        verify(projectRepository).existsByNameIgnoreCase(request.name());
        verifyNoMoreInteractions(projectRepository, projectMapper);
    }

    @Test
    void should_throw_duplicate_project_exception_when_persisting_duplicate_project() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", "Active");
        var entity = newProject("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);

        when(projectRepository.existsByNameIgnoreCase(request.name())).thenReturn(false);
        when(projectMapper.toEntity(request)).thenReturn(entity);
        when(projectRepository.save(entity)).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> projectService.create(request))
            .isInstanceOf(DuplicateProjectException.class)
            .hasMessage("Project name Atlas Migration is already taken");
    }

    @Test
    void should_return_projects_in_created_at_descending_order_when_listing_projects() {
        var newest = newProject("Newest Project", "Owner One", ProjectStatus.BLOCKED);
        var older = newProject("Older Project", "Owner Two", ProjectStatus.ACTIVE);
        var newestResponse = new ProjectResponse(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174001"),
            "Newest Project",
            "Owner One",
            "Blocked",
            Instant.parse("2024-01-03T10:15:30Z"));
        var olderResponse = new ProjectResponse(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174002"),
            "Older Project",
            "Owner Two",
            "Active",
            Instant.parse("2024-01-02T10:15:30Z"));

        when(projectRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(newest, older));
        when(projectMapper.toResponse(newest)).thenReturn(newestResponse);
        when(projectMapper.toResponse(older)).thenReturn(olderResponse);

        var result = projectService.getAll();

        assertThat(result).containsExactly(newestResponse, olderResponse);
    }

    @Test
    void should_return_project_when_identifier_exists() {
        var id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        var entity = newProject("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        entity.setId(id);
        entity.setCreatedAt(Instant.parse("2024-01-01T10:15:30Z"));
        var response = new ProjectResponse(id, "Atlas Migration", "Jane Doe", "Active", entity.getCreatedAt());

        when(projectRepository.findById(id)).thenReturn(java.util.Optional.of(entity));
        when(projectMapper.toResponse(entity)).thenReturn(response);

        var result = projectService.getById(id);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void should_throw_not_found_exception_when_project_identifier_is_missing() {
        var id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(projectRepository.findById(id)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> projectService.getById(id))
            .isInstanceOf(ProjectNotFoundException.class)
            .hasMessage("Project with id 123e4567-e89b-12d3-a456-426614174000 was not found");
    }

    @Test
    void should_return_dashboard_summary_from_repository_counts() {
        when(projectRepository.count()).thenReturn(6L);
        when(projectRepository.countByStatus(ProjectStatus.ACTIVE)).thenReturn(3L);
        when(projectRepository.countByStatus(ProjectStatus.AT_RISK)).thenReturn(2L);
        when(projectRepository.countByStatus(ProjectStatus.BLOCKED)).thenReturn(1L);
        when(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).thenReturn(0L);

        var result = projectService.getDashboardSummary();

        assertThat(result).isEqualTo(new DashboardSummary(6L, 3L, 2L, 1L, 0L));
    }

    private static Project newProject(String name, String ownerName, ProjectStatus status) {
        var project = newProject();
        project.setName(name);
        project.setOwnerName(ownerName);
        project.setStatus(status);
        return project;
    }

    private static Project newProject() {
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
