package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.repository.ProjectRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        throw new UnsupportedOperationException("Project creation is not implemented yet");
    }

    @Override
    public List<ProjectResponse> listAll() {
        throw new UnsupportedOperationException("Project listing is not implemented yet");
    }

    @Override
    public ProjectResponse getById(UUID id) {
        throw new UnsupportedOperationException("Project lookup is not implemented yet");
    }

    @Override
    public DashboardSummary getDashboardSummary() {
        throw new UnsupportedOperationException("Dashboard summary is not implemented yet");
    }
}
