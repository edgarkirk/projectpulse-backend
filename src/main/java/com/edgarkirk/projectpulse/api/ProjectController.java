package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.DashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.service.ProjectService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectResponse> createProject(@RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request));
    }

    @GetMapping("/projects")
    public List<ProjectResponse> listProjects() {
        return projectService.listAll();
    }

    @GetMapping("/projects/{id}")
    public ProjectResponse getProjectById(@PathVariable UUID id) {
        return projectService.getById(id);
    }

    @GetMapping("/dashboard/summary")
    public DashboardSummary getDashboardSummary() {
        return projectService.getDashboardSummary();
    }
}
