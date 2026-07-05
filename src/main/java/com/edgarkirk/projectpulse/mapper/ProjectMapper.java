package com.edgarkirk.projectpulse.mapper;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;

import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public Project toEntity(CreateProjectRequest request) {
        var project = newProject();
        project.setName(request.name());
        project.setOwnerName(request.ownerName());
        project.setStatus(ProjectStatus.fromDisplayName(request.status()));
        return project;
    }

    private Project newProject() {
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create Project entity", exception);
        }
    }

    public ProjectResponse toResponse(Project entity) {
        return new ProjectResponse(
            entity.getId(),
            entity.getName(),
            entity.getOwnerName(),
            entity.getStatus().getDisplayName(),
            entity.getCreatedAt());
    }
}
