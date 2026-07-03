package com.edgarkirk.projectpulse.api.dto.response;

public record DashboardSummary(int totalProjects, int active, int atRisk, int blocked, int onHold) {
}
