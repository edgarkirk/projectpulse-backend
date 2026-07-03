package com.edgarkirk.projectpulse.mapper;

import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId().toString(),
                project.getName(),
                project.getOwnerName(),
                project.getStatus(),
                project.getCreatedAt());
    }
}
