package com.edgarkirk.projectpulse.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
    UUID id,
    String name,
    String ownerName,
    String status,
    Instant createdAt
) {
}
