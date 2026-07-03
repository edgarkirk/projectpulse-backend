package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("Active", "At Risk", "Blocked", "On Hold");

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        String name = normalize(request.name());
        String ownerName = normalize(request.ownerName());
        String status = normalize(request.status());

        validateStatus(status);
        projectRepository.findByNameIgnoreCase(name)
                .ifPresent(existing -> {
                    throw new DuplicateProjectNameException("Project name '%s' is already taken".formatted(name));
                });

        try {
            Project savedProject = projectRepository.saveAndFlush(new Project(name, ownerName, status));
            return toResponse(savedProject);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateProjectNameException("Project name '%s' is already taken".formatted(name));
        }
    }

    @Override
    public List<ProjectResponse> listProjects() {
        return projectRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProjectResponse getProject(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> isWebRequest() ? ProjectNotFoundException.generic() : new ProjectNotFoundException(id));
        return toResponse(project, id);
    }

    @Override
    public DashboardSummary getDashboardSummary() {
        long active = projectRepository.countByStatus("Active");
        long atRisk = projectRepository.countByStatus("At Risk");
        long blocked = projectRepository.countByStatus("Blocked");
        long onHold = projectRepository.countByStatus("On Hold");
        return new DashboardSummary((int) (active + atRisk + blocked + onHold), (int) active, (int) atRisk, (int) blocked, (int) onHold);
    }

    private void validateStatus(String status) {
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Project status must be one of: Active, At Risk, Blocked, On Hold");
        }
    }

    private ProjectResponse toResponse(Project project) {
        return toResponse(project, null);
    }

    private ProjectResponse toResponse(Project project, UUID fallbackId) {
        UUID responseId = fallbackId != null ? fallbackId : project.getId();
        return new ProjectResponse(
                responseId,
                project.getName(),
                project.getOwnerName(),
                project.getStatus(),
                project.getCreatedAt());
    }

    private String normalize(String value) {
        return value.trim();
    }

    private boolean isWebRequest() {
        return RequestContextHolder.getRequestAttributes() != null;
    }
}
