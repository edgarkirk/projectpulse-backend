package com.edgarkirk.projectpulse.api.dto.response;

public record DashboardSummary(
        Long totalProjects,
        Long active,
        Long atRisk,
        Long blocked,
        Long onHold
) {
}
