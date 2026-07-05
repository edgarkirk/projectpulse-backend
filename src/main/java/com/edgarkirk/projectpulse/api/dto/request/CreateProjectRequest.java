package com.edgarkirk.projectpulse.api.dto.request;

import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required")
        @Size(max = 100, message = "Project name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Owner name is required")
        @Size(max = 100, message = "Owner name must not exceed 100 characters")
        String ownerName,

        @NotNull(message = "Status is required")
        ProjectStatus status) {
}
