package com.edgarkirk.projectpulse.service.exception;

public class DuplicateProjectNameException extends RuntimeException {

    public DuplicateProjectNameException(String message) {
        super(message);
    }
}
