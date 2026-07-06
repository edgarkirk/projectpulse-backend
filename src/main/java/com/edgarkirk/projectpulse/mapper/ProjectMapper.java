package com.edgarkirk.projectpulse.mapper;


import org.springframework.stereotype.Component;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;

@Component
public class ProjectMapper {

    public Project toEntity(CreateProjectRequest request) {
        var project = instantiateProject();
        project.setName(request.name().trim());
        project.setOwnerName(request.ownerName().trim());
        project.setStatus(request.status());
        return project;
    }

    private Project instantiateProject() {
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create Project entity", e);
        }
    }

    public ProjectResponse toResponse(Project entity) {
        return new ProjectResponse(
                entity.getId(),
                entity.getName(),
                entity.getOwnerName(),
                entity.getStatus().value(),
                entity.getCreatedAt());
    }
}
