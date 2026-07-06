package com.edgarkirk.projectpulse.api.dto.request;

import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name exceeds the maximum length of 100 characters")
        String name,
        @NotBlank(message = "ownerName is required")
        @Size(max = 100, message = "ownerName exceeds the maximum length of 100 characters")
        String ownerName,
        @NotNull(message = "status is required")
        ProjectStatus status) {
}
