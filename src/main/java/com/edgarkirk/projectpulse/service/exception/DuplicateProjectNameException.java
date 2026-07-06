package com.edgarkirk.projectpulse.service.exception;

public class DuplicateProjectNameException extends RuntimeException {

    public DuplicateProjectNameException(String name) {
        super("project name already exists: " + name);
    }
}
