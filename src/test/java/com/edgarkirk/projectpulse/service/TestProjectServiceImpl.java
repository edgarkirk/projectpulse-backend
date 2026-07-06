package com.edgarkirk.projectpulse.service;

import java.util.List;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.TestCreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.TestDashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.TestProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.TestProjectEntity;
import com.edgarkirk.projectpulse.persistence.repository.TestProjectRepository;

public class TestProjectServiceImpl implements TestProjectService {

    private final TestProjectRepository repository;

    public TestProjectServiceImpl(TestProjectRepository repository) {
        this.repository = repository;
    }

    @Override
    public TestProjectResponse create(TestCreateProjectRequest request) {
        var saved = repository.save(new TestProjectEntity(
                null,
                request.name(),
                request.ownerName(),
                request.status().value(),
                null));
        return toResponse(saved);
    }

    @Override
    public List<TestProjectResponse> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Override
    public TestProjectResponse findById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalStateException("Project not found"));
    }

    @Override
    public TestDashboardSummary getDashboardSummary() {
        return new TestDashboardSummary(
                (int) repository.count(),
                (int) repository.countByStatus("Active"),
                0,
                0,
                0);
    }

    private TestProjectResponse toResponse(TestProjectEntity entity) {
        return new TestProjectResponse(
                entity.getId(),
                entity.getName(),
                entity.getOwnerName(),
                com.edgarkirk.projectpulse.api.dto.request.TestProjectStatus.fromValue(entity.getStatus()),
                entity.getCreatedAt());
    }
}
