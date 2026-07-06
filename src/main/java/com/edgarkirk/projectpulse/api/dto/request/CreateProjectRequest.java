package com.edgarkirk.projectpulse.api.dto.request;

import com.edgarkirk.projectpulse.domain.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,
        @NotBlank(message = "ownerName is required")
        @Size(max = 100, message = "ownerName must be at most 100 characters")
        String ownerName,
        @NotNull(message = "status is required")
        ProjectStatus status
) {
}
