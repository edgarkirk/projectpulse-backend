package com.edgarkirk.projectpulse.service;

import java.util.List;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.request.ProjectStatus;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.mapper.ProjectMapper;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        try {
            Project project = projectMapper.toEntity(request);
            return projectMapper.toResponse(projectRepository.saveAndFlush(project));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateProjectNameException("Project name '" + request.name().trim() + "' already exists");
        }
    }

    @Override
    public List<ProjectResponse> findAll() {
        return projectRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Override
    public ProjectResponse findById(UUID id) {
        return projectRepository.findById(id)
                .map(projectMapper::toResponse)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + id + " not found"));
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
