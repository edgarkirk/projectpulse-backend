package com.edgarkirk.projectpulse.service;

import java.util.List;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;

public interface ProjectService {

    ProjectResponse create(CreateProjectRequest request);

    List<ProjectResponse> findAll();

    ProjectResponse findById(UUID id);

    DashboardSummary getDashboardSummary();
}
