package com.edgarkirk.projectpulse.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required.")
        @Size(max = 100, message = "Project name must not exceed 100 characters.")
        String name,

        @NotBlank(message = "Owner name is required.")
        @Size(max = 100, message = "Owner name must not exceed 100 characters.")
        String ownerName,

        @NotBlank(message = "Project status is required.")
        @Pattern(regexp = "Active|At Risk|Blocked|On Hold", message = "Project status must be one of Active, At Risk, Blocked, or On Hold.")
        String status) {
}
