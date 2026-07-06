package com.edgarkirk.projectpulse.service;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectResponse create(CreateProjectRequest request);

    List<ProjectResponse> listAll();

    ProjectResponse getById(UUID id);

    DashboardSummary getDashboardSummary();
}
