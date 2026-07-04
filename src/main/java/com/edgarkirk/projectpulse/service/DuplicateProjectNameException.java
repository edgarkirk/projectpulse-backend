package com.edgarkirk.projectpulse.service;

public class DuplicateProjectNameException extends RuntimeException {

    public DuplicateProjectNameException(String message) {
        super(message);
    }

    public DuplicateProjectNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
