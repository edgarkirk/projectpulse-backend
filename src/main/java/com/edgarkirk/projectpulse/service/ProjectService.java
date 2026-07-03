package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummaryResponse;
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
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<ProjectResponse> listProjects() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ProjectResponse getProjectById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public DashboardSummaryResponse getDashboardSummary() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected ProjectRepository getProjectRepository() {
        return projectRepository;
    }
}
