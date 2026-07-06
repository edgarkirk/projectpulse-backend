package com.edgarkirk.projectpulse.api;

import java.util.List;
import java.util.UUID;

import com.edgarkirk.projectpulse.api.dto.request.TestCreateProjectRequest;
import com.edgarkirk.projectpulse.api.dto.response.TestDashboardSummary;
import com.edgarkirk.projectpulse.api.dto.response.TestProjectResponse;
import com.edgarkirk.projectpulse.service.TestProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TestProjectApiController {

    private final TestProjectService service;

    public TestProjectApiController(TestProjectService service) {
        this.service = service;
    }

    @PostMapping("/api/projects")
    public ResponseEntity<TestProjectResponse> create(@RequestBody TestCreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/api/projects")
    public List<TestProjectResponse> list() {
        return service.findAll();
    }

    @GetMapping("/api/projects/{id}")
    public TestProjectResponse getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping("/api/dashboard/summary")
    public TestDashboardSummary summary() {
        return service.getDashboardSummary();
    }
}
