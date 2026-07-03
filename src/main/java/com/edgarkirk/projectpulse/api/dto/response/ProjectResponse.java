package com.edgarkirk.projectpulse.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponse(
        @Schema(description = "Project UUID", format = "uuid") UUID id,
        String name,
        String ownerName,
        @Schema(description = "Canonical project status") String status,
        OffsetDateTime createdAt) {
}
