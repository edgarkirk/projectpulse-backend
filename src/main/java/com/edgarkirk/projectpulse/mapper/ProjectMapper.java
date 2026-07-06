package com.edgarkirk.projectpulse.mapper;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public Project toEntity(CreateProjectRequest request) {
        return new Project(request.name(), request.ownerName(), request.status(), null);
    }

    public ProjectResponse toResponse(Project entity) {
        return new ProjectResponse(
                entity.getId(),
                entity.getName(),
                entity.getOwnerName(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
