package com.edgarkirk.projectpulse.api.dto.response;

public record DashboardSummary(long totalProjects, long active, long atRisk, long blocked, long onHold) {
}
