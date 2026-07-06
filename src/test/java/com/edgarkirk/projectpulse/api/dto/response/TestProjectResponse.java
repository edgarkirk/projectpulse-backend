package com.edgarkirk.projectpulse.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.TestProjectStatus;

public record TestProjectResponse(
        UUID id,
        String name,
        String ownerName,
        TestProjectStatus status,
        OffsetDateTime createdAt) {
}
