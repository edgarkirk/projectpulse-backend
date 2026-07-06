package com.edgarkirk.projectpulse.service;

import java.util.List;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.TestCreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.TestDashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.TestProjectResponse;

public interface TestProjectService {

    TestProjectResponse create(TestCreateProjectRequest request);

    List<TestProjectResponse> findAll();

    TestProjectResponse findById(UUID id);

    TestDashboardSummary getDashboardSummary();
}
