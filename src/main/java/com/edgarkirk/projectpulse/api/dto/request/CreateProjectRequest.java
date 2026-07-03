package com.edgarkirk.projectpulse.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name exceeds maximum length of 100")
        String name,
        @NotBlank(message = "ownerName is required")
        @Size(max = 100, message = "ownerName exceeds maximum length of 100")
        String ownerName,
        @NotBlank(message = "status is required")
        @Pattern(regexp = "Active|At Risk|Blocked|On Hold", message = "status must be one of Active, At Risk, Blocked, On Hold")
        String status) {
}
