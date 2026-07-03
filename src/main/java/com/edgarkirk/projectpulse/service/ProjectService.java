package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    List<ProjectResponse> listProjects();

    ProjectResponse getProject(UUID id);

    DashboardSummary getDashboardSummary();
}
