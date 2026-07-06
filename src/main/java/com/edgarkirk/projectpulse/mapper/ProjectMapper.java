package com.edgarkirk.projectpulse.mapper;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public Project toEntity(CreateProjectRequest request) {
        var project = org.springframework.beans.BeanUtils.instantiateClass(Project.class);
        project.setName(request.name());
        project.setOwnerName(request.ownerName());
        project.setStatus(request.status());
        return project;
    }

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getOwnerName(),
                project.getStatus().getApiValue(),
                project.getCreatedAt());
    }
}
