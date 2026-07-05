package com.edgarkirk.projectpulse.service;

import java.util.List;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.mapper.ProjectMapper;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        if (projectRepository.existsByNameIgnoreCase(request.name())) {
            throw duplicateProjectException(request.name());
        }

        try {
            var project = projectMapper.toEntity(request);
            var savedProject = projectRepository.save(project);
            return projectMapper.toResponse(savedProject);
        } catch (DataIntegrityViolationException exception) {
            throw duplicateProjectException(request.name());
        }
    }

    @Override
    public List<ProjectResponse> getAll() {
        return projectRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(projectMapper::toResponse)
            .toList();
    }

    @Override
    public ProjectResponse getById(UUID id) {
        var project = projectRepository.findById(id)
            .orElseThrow(() -> new ProjectNotFoundException(
                "Project with id " + id + " was not found"));
        return projectMapper.toResponse(project);
    }

    @Override
    public DashboardSummary getDashboardSummary() {
        return new DashboardSummary(
            projectRepository.count(),
            projectRepository.countByStatus(ProjectStatus.ACTIVE),
            projectRepository.countByStatus(ProjectStatus.AT_RISK),
            projectRepository.countByStatus(ProjectStatus.BLOCKED),
            projectRepository.countByStatus(ProjectStatus.ON_HOLD));
    }

    private DuplicateProjectException duplicateProjectException(String projectName) {
        return new DuplicateProjectException("Project name " + projectName + " is already taken");
    }
}
