package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        throw new UnsupportedOperationException("Project creation is not implemented yet");
    }

    @Override
    public List<ProjectResponse> listProjects() {
        throw new UnsupportedOperationException("Project listing is not implemented yet");
    }

    @Override
    public ProjectResponse getProject(UUID id) {
        throw new UnsupportedOperationException("Project lookup is not implemented yet");
    }

    @Override
    public DashboardSummary getDashboardSummary() {
        throw new UnsupportedOperationException("Dashboard summary is not implemented yet");
    }
}
