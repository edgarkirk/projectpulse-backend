package com.edgarkirk.projectpulse.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.mapper.ProjectMapper;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;

@Service
@Transactional(readOnly = true)
class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private static final String ACTIVE_STATUS = "Active";
    private static final String AT_RISK_STATUS = "At Risk";
    private static final String BLOCKED_STATUS = "Blocked";
    private static final String ON_HOLD_STATUS = "On Hold";
    private static final String DUPLICATE_PROJECT_MESSAGE = "Project name is already taken";

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        String trimmedName = request.name().trim();
        String trimmedOwnerName = request.ownerName().trim();
        String status = request.status().trim();

        log.info("Creating project with name '{}'", trimmedName);

        if (projectRepository.findByNameIgnoreCase(trimmedName).isPresent()) {
            log.warn("Rejected duplicate project name '{}'", trimmedName);
            throw new DuplicateProjectNameException(DUPLICATE_PROJECT_MESSAGE);
        }

        Project project = projectMapper.toEntity(new CreateProjectRequest(trimmedName, trimmedOwnerName, status));

        try {
            Project savedProject = projectRepository.save(project);
            return projectMapper.toResponse(savedProject);
        } catch (DataIntegrityViolationException exception) {
            log.warn("Database rejected duplicate project name '{}'", trimmedName, exception);
            throw new DuplicateProjectNameException(DUPLICATE_PROJECT_MESSAGE, exception);
        }
    }

    @Override
    public List<ProjectResponse> listProjects() {
        log.info("Listing all projects ordered by created date descending");
        return projectMapper.toResponses(projectRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public ProjectResponse getProjectById(UUID id) {
        log.info("Fetching project with id '{}'", id);
        return projectRepository.findById(id)
                .map(projectMapper::toResponse)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id %s was not found".formatted(id)));
    }

    @Override
    public DashboardSummary getDashboardSummary() {
        log.info("Computing dashboard summary from current database state");
        return new DashboardSummary(
                projectRepository.count(),
                projectRepository.countByStatus(ACTIVE_STATUS),
                projectRepository.countByStatus(AT_RISK_STATUS),
                projectRepository.countByStatus(BLOCKED_STATUS),
                projectRepository.countByStatus(ON_HOLD_STATUS));
    }
}
