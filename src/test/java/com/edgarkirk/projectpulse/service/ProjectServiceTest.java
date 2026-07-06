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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void should_return_created_project_when_name_is_unique() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        var entity = new Project(null, "Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, null);
        var saved = new Project(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, OffsetDateTime.parse("2026-01-01T10:15:30Z"));
        var response = new ProjectResponse(saved.getId(), saved.getName(), saved.getOwnerName(), "Active", saved.getCreatedAt());

        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(false);
        when(projectMapper.toEntity(request)).thenReturn(entity);
        when(projectRepository.save(entity)).thenReturn(saved);
        when(projectMapper.toResponse(saved)).thenReturn(response);

        var result = projectService.create(request);

        assertThat(result).isEqualTo(response);
        verify(projectRepository).save(entity);
    }

    @Test
    void should_throw_duplicate_project_name_exception_when_name_exists_case_insensitively() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(true);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessage("project name already exists: Atlas Migration");
    }

    @Test
    void should_throw_duplicate_project_name_exception_when_save_detects_race_condition() {
        var request = new CreateProjectRequest("Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE);
        var entity = new Project(null, "Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, null);

        when(projectRepository.existsByNameIgnoreCase("Atlas Migration")).thenReturn(false);
        when(projectMapper.toEntity(request)).thenReturn(entity);
        when(projectRepository.save(entity)).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessage("project name already exists: Atlas Migration");
    }

    @Test
    void should_return_project_when_id_exists() {
        var id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        var entity = new Project(id, "Atlas Migration", "Jane Doe", ProjectStatus.ACTIVE, OffsetDateTime.parse("2026-01-01T10:15:30Z"));
        var response = new ProjectResponse(id, "Atlas Migration", "Jane Doe", "Active", entity.getCreatedAt());

        when(projectRepository.findById(id)).thenReturn(Optional.of(entity));
        when(projectMapper.toResponse(entity)).thenReturn(response);

        var result = projectService.getById(id);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void should_throw_project_not_found_exception_when_project_does_not_exist() {
        var id = UUID.fromString("33333333-3333-3333-3333-333333333333");
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getById(id))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("project not found for id 33333333-3333-3333-3333-333333333333");
    }

    @Test
    void should_return_projects_in_newest_first_order_when_listing_all() {
        var newest = new Project(UUID.fromString("44444444-4444-4444-4444-444444444444"), "Newest Project", "Jane Doe", ProjectStatus.ACTIVE, OffsetDateTime.parse("2026-01-02T10:15:30Z"));
        var older = new Project(UUID.fromString("55555555-5555-5555-5555-555555555555"), "Older Project", "John Doe", ProjectStatus.BLOCKED, OffsetDateTime.parse("2026-01-01T10:15:30Z"));
        var newestResponse = new ProjectResponse(newest.getId(), newest.getName(), newest.getOwnerName(), "Active", newest.getCreatedAt());
        var olderResponse = new ProjectResponse(older.getId(), older.getName(), older.getOwnerName(), "Blocked", older.getCreatedAt());

        when(projectRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(newest, older));
        when(projectMapper.toResponse(newest)).thenReturn(newestResponse);
        when(projectMapper.toResponse(older)).thenReturn(olderResponse);

        var result = projectService.findAll();

        assertThat(result).containsExactly(newestResponse, olderResponse);
    }

    @Test
    void should_return_dashboard_summary_when_projects_exist() {
        when(projectRepository.count()).thenReturn(6L);
        when(projectRepository.countByStatus(ProjectStatus.ACTIVE)).thenReturn(3L);
        when(projectRepository.countByStatus(ProjectStatus.AT_RISK)).thenReturn(2L);
        when(projectRepository.countByStatus(ProjectStatus.BLOCKED)).thenReturn(1L);
        when(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).thenReturn(0L);

        var result = projectService.getDashboardSummary();

        assertThat(result).isEqualTo(new DashboardSummary(6L, 3L, 2L, 1L, 0L));
    }
}
