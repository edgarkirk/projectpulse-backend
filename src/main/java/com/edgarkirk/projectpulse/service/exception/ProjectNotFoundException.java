package com.edgarkirk.projectpulse.service.exception;

import java.util.UUID;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(UUID id) {
        super("Project with id %s was not found".formatted(id));
    }

    private ProjectNotFoundException(String message) {
        super(message);
    }

    public static ProjectNotFoundException generic() {
        return new ProjectNotFoundException("Project not found");
    }
}
