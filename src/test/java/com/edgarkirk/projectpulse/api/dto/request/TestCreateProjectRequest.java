package com.edgarkirk.projectpulse.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TestCreateProjectRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String ownerName,
        @NotNull TestProjectStatus status) {
}
