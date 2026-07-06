package com.edgarkirk.projectpulse.api.dto.response;

public record TestDashboardSummary(
        int totalProjects,
        int active,
        int atRisk,
        int blocked,
        int onHold) {
}
