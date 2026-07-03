package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management endpoints")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @GetMapping
    @Operation(summary = "List all projects")
    public ResponseEntity<List<ProjectResponse>> listProjects() {
        return ResponseEntity.ok(projectService.listProjects());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by ID")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }
}
