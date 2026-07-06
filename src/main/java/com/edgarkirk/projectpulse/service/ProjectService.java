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
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        var name = normalize(request.name());
        var ownerName = normalize(request.ownerName());

        if (projectRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateProjectNameException(name);
        }

        var project = projectMapper.toEntity(new CreateProjectRequest(name, ownerName, request.status()));
        try {
            return projectMapper.toResponse(projectRepository.saveAndFlush(project));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateProjectNameException(name);
        }
    }

    public List<ProjectResponse> listProjects() {
        return projectRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    public ProjectResponse getProject(UUID id) {
        return projectRepository.findById(id)
                .map(projectMapper::toResponse)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    public DashboardSummary getDashboardSummary() {
        long totalProjects = projectRepository.count();
        long active = projectRepository.countByStatus(ProjectStatus.ACTIVE);
        long atRisk = projectRepository.countByStatus(ProjectStatus.AT_RISK);
        long blocked = projectRepository.countByStatus(ProjectStatus.BLOCKED);
        long onHold = projectRepository.countByStatus(ProjectStatus.ON_HOLD);
        return new DashboardSummary(totalProjects, active, atRisk, blocked, onHold);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
