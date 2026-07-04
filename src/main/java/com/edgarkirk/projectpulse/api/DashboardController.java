package com.edgarkirk.projectpulse.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Project dashboard summary")
class DashboardController {

    private final ProjectService projectService;

    DashboardController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary with project counts per status")
    DashboardSummary getSummary() {
        return projectService.getDashboardSummary();
    }
}
