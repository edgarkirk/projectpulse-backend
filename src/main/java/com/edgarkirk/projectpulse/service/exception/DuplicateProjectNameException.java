package com.edgarkirk.projectpulse.service.exception;

public class DuplicateProjectNameException extends RuntimeException {

    public DuplicateProjectNameException(String projectName) {
        super("Project name '" + projectName + "' is already taken.");
    }
}
