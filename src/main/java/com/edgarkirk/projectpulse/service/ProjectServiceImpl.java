package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.mapper.ProjectMapper;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private static final String DUPLICATE_PROJECT_MESSAGE = "Project name already taken";
    private static final String PROJECT_NOT_FOUND_MESSAGE = "Project not found";

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        var normalizedRequest = new CreateProjectRequest(
                request.name().trim(),
                request.ownerName().trim(),
                request.status());

        if (projectRepository.existsByNameIgnoreCase(normalizedRequest.name())) {
            throw new DuplicateProjectNameException(DUPLICATE_PROJECT_MESSAGE);
        }

        try {
            Project savedProject = projectRepository.saveAndFlush(projectMapper.toEntity(normalizedRequest));
            return projectMapper.toResponse(savedProject);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateProjectNameException(DUPLICATE_PROJECT_MESSAGE);
        }
    }

    @Override
    public List<ProjectResponse> listAll() {
        return projectRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Override
    public ProjectResponse getById(UUID id) {
        return projectRepository.findById(id)
                .map(projectMapper::toResponse)
                .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_FOUND_MESSAGE));
    }

    @Override
    public DashboardSummary getDashboardSummary() {
        return new DashboardSummary(
                Math.toIntExact(projectRepository.count()),
                Math.toIntExact(projectRepository.countByStatus(ProjectStatus.ACTIVE)),
                Math.toIntExact(projectRepository.countByStatus(ProjectStatus.AT_RISK)),
                Math.toIntExact(projectRepository.countByStatus(ProjectStatus.BLOCKED)),
                Math.toIntExact(projectRepository.countByStatus(ProjectStatus.ON_HOLD)));
    }
}
