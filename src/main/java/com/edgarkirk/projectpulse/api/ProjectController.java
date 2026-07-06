package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.request.CreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.ProjectResponse;
import com.edgarkirk.projectpulse.service.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectController {

    private final ProjectService projectService;
    private final ObjectMapper objectMapper;

    public ProjectController(ProjectService projectService, ObjectMapper objectMapper) {
        this.projectService = projectService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<ProjectResponse> createProjectFromText(@RequestBody String body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(objectMapper.readValue(body, CreateProjectRequest.class)));
        } catch (JsonProcessingException exception) {
            var message = exception.getCause() instanceof IllegalArgumentException illegalArgumentException && illegalArgumentException.getMessage() != null
                    ? illegalArgumentException.getMessage()
                    : "request body is malformed";
            throw new IllegalArgumentException(message);
        }
    }

    @GetMapping
    public List<ProjectResponse> listProjects() {
        return projectService.listProjects();
    }

    @GetMapping("/{id}")
    public ProjectResponse getProject(@PathVariable UUID id) {
        return projectService.getProject(id);
    }
}
