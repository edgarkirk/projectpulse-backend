package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
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

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        try {
            Project savedProject = projectRepository.saveAndFlush(new Project(
                    request.name(),
                    request.ownerName(),
                    request.status(),
                    null));
            return toResponse(savedProject);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateProjectNameException(request.name());
        }
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProjectById(UUID projectId) {
        return projectRepository.findById(projectId)
                .map(this::toResponse)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    public DashboardSummary getDashboardSummary() {
        long totalProjects = projectRepository.count();
        int active = Math.toIntExact(projectRepository.countByStatus("Active"));
        int atRisk = Math.toIntExact(projectRepository.countByStatus("At Risk"));
        int blocked = Math.toIntExact(projectRepository.countByStatus("Blocked"));
        int onHold = Math.toIntExact(projectRepository.countByStatus("On Hold"));
        return new DashboardSummary(Math.toIntExact(totalProjects), active, atRisk, blocked, onHold);
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getOwnerName(),
                project.getStatus(),
                project.getCreatedAt());
    }
}
