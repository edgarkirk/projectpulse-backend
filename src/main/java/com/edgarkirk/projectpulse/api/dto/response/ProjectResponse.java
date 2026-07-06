package com.edgarkirk.projectpulse.api.dto.response;

import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponse(UUID id, String name, String ownerName, ProjectStatus status, OffsetDateTime createdAt) {
}
