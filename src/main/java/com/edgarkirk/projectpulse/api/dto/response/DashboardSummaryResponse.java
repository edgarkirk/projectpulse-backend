package com.edgarkirk.projectpulse.api.dto.response;

public record DashboardSummaryResponse(
        long totalProjects,
        long active,
        long atRisk,
        long blocked,
        long onHold) {
}
