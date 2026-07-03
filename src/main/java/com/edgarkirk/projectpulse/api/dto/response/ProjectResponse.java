package com.edgarkirk.projectpulse.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record ProjectResponse(
        String id,
        String name,
        String ownerName,
        @Schema(description = "Canonical project status") String status,
        OffsetDateTime createdAt) {
}
