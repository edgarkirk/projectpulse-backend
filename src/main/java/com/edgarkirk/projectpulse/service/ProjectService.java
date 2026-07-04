package com.edgarkirk.projectpulse.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;

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
            throw new DuplicateProjectNameException("Project name '%s' is already taken.".formatted(request.name()));
        }

        Project project = new Project(request.name(), request.ownerName(), ProjectStatus.fromApiValue(request.status()));

        try {
            return toResponse(projectRepository.save(project));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateProjectNameException("Project name '%s' is already taken.".formatted(request.name()), exception);
        }
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProjectById(UUID id) {
        return projectRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    public DashboardSummary getDashboardSummary() {
        long active = projectRepository.countByStatus(ProjectStatus.ACTIVE);
        long atRisk = projectRepository.countByStatus(ProjectStatus.AT_RISK);
        long blocked = projectRepository.countByStatus(ProjectStatus.BLOCKED);
        long onHold = projectRepository.countByStatus(ProjectStatus.ON_HOLD);
        return new DashboardSummary((int) (active + atRisk + blocked + onHold), (int) active, (int) atRisk, (int) blocked, (int) onHold);
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId() != null ? project.getId() : UUID.randomUUID(),
                project.getName(),
                project.getOwnerName(),
                project.getStatus().getDisplayName(),
                project.getCreatedAt() != null ? OffsetDateTime.ofInstant(project.getCreatedAt(), ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC));
    }
}
