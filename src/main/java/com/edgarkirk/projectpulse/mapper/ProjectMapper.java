package com.edgarkirk.projectpulse.mapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;

class ProjectMapper {

    Project toEntity(CreateProjectRequest request) {
        return new Project(request.name(), request.ownerName(), ProjectStatus.fromApiValue(request.status()));
    }

    ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId() != null ? project.getId() : UUID.randomUUID(),
                project.getName(),
                project.getOwnerName(),
                project.getStatus().getDisplayName(),
                project.getCreatedAt() != null ? OffsetDateTime.ofInstant(project.getCreatedAt(), ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC));
    }

    DashboardSummary toDashboardSummary(long active, long atRisk, long blocked, long onHold) {
        return new DashboardSummary((int) (active + atRisk + blocked + onHold), (int) active, (int) atRisk, (int) blocked, (int) onHold);
    }
}
