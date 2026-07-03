package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectResponse createProject(CreateProjectRequest request) {
        if (projectRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateProjectNameException("Project name '" + request.name() + "' is already taken");
        }

        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName(request.name());
        project.setOwnerName(request.ownerName());
        project.setStatus(ProjectStatus.fromWireValue(request.status()));
        project.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        projectRepository.save(project);
        return toResponse(project);
    }

    public List<ProjectResponse> listProjects() {
        return projectRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project '" + id + "' was not found"));
        return toResponse(project);
    }

    public DashboardSummary getDashboardSummary() {
        return new DashboardSummary(
                Math.toIntExact(projectRepository.count()),
                Math.toIntExact(projectRepository.countByStatus(ProjectStatus.ACTIVE)),
                Math.toIntExact(projectRepository.countByStatus(ProjectStatus.AT_RISK)),
                Math.toIntExact(projectRepository.countByStatus(ProjectStatus.BLOCKED)),
                Math.toIntExact(projectRepository.countByStatus(ProjectStatus.ON_HOLD))
        );
    }

    protected ProjectRepository getProjectRepository() {
        return projectRepository;
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getOwnerName(),
                project.getStatus().getWireValue(),
                project.getCreatedAt()
        );
    }
}
