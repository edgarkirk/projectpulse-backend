package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectResponse createProject(CreateProjectRequest request) {
        throw new UnsupportedOperationException("ProjectService#createProject is not implemented yet");
    }

    public List<ProjectResponse> listProjects() {
        throw new UnsupportedOperationException("ProjectService#listProjects is not implemented yet");
    }

    public ProjectResponse getProjectById(UUID id) {
        throw new UnsupportedOperationException("ProjectService#getProjectById is not implemented yet");
    }

    public DashboardSummary getDashboardSummary() {
        throw new UnsupportedOperationException("ProjectService#getDashboardSummary is not implemented yet");
    }

    protected ProjectRepository getProjectRepository() {
        return projectRepository;
    }
}
