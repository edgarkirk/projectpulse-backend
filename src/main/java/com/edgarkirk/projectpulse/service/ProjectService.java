package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummaryResponse;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        if (projectRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateProjectNameException(request.name());
        }

        Project project = new Project(null, request.name(), request.ownerName(), ProjectStatus.fromDisplayName(request.status()), null);
        try {
            return toResponse(projectRepository.save(project));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateProjectNameException(request.name());
        }
    }

    public List<ProjectResponse> listProjects() {
        return projectRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProjectById(UUID id) {
        return projectRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    public DashboardSummaryResponse getDashboardSummary() {
        return new DashboardSummaryResponse(
                projectRepository.count(),
                projectRepository.countByStatus(ProjectStatus.ACTIVE),
                projectRepository.countByStatus(ProjectStatus.AT_RISK),
                projectRepository.countByStatus(ProjectStatus.BLOCKED),
                projectRepository.countByStatus(ProjectStatus.ON_HOLD));
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getOwnerName(),
                project.getStatus().getDisplayName(),
                project.getCreatedAt());
    }
}
