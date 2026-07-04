package com.edgarkirk.projectpulse.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.persistence.entity.Project;

@Component
public class ProjectMapper {

    public Project toEntity(CreateProjectRequest request) {
        return new Project(request.name(), request.ownerName(), request.status());
    }

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getOwnerName(), project.getStatus(), project.getCreatedAt());
    }

    public List<ProjectResponse> toResponses(List<Project> projects) {
        return projects.stream().map(this::toResponse).toList();
    }
}
