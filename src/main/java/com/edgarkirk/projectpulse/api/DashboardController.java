package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.response.DashboardSummaryResponse;
import com.edgarkirk.projectpulse.service.ProjectService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Validated
class DashboardController {

    private final ProjectService projectService;

    public DashboardController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return projectService.getDashboardSummary();
    }
}
