package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.response.ErrorResponse;
import com.edgarkirk.projectpulse.persistence.entity.ProjectStatus;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ProjectExceptionHandler {

    private static final String DUPLICATE_PROJECT_MESSAGE = "Project name already taken";
    private static final String PROJECT_NOT_FOUND_MESSAGE = "Project not found";
    private static final String MALFORMED_JSON_MESSAGE = "Malformed JSON request body";
    private static final String INVALID_STATUS_MESSAGE = "status must be one of Active, At Risk, Blocked, On Hold";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(firstFieldErrorMessage(exception.getBindingResult())));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                exception.getConstraintViolations().stream()
                        .findFirst()
                        .map(violation -> violation.getMessage())
                        .orElse("Request validation failed")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        if (isInvalidStatusValue(exception)) {
            return ResponseEntity.badRequest().body(new ErrorResponse(INVALID_STATUS_MESSAGE));
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(MALFORMED_JSON_MESSAGE));
    }

    @ExceptionHandler(DuplicateProjectNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateProjectName(DuplicateProjectNameException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFound(ProjectNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        if (UUID.class.equals(exception.getRequiredType())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("id must be a valid UUID"));
        }
        return ResponseEntity.badRequest().body(new ErrorResponse("Request parameter is invalid"));
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(DUPLICATE_PROJECT_MESSAGE));
    }

    private String firstFieldErrorMessage(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Request validation failed");
    }

    private boolean isInvalidStatusValue(HttpMessageNotReadableException exception) {
        Throwable current = exception.getMostSpecificCause();
        while (current != null) {
            if (current instanceof InvalidFormatException invalidFormatException
                    && invalidFormatException.getTargetType() == ProjectStatus.class) {
                return true;
            }
            if (current instanceof IllegalArgumentException illegalArgumentException
                    && INVALID_STATUS_MESSAGE.equals(illegalArgumentException.getMessage())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}