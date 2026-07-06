package com.edgarkirk.projectpulse.api;

import com.edgarkirk.projectpulse.api.dto.response.ErrorResponse;
import com.edgarkirk.projectpulse.service.exception.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.exception.ProjectNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(Exception exception) {
        var message = "validation failed";
        if (exception instanceof MethodArgumentNotValidException manve && !manve.getBindingResult().getFieldErrors().isEmpty()) {
            message = Objects.requireNonNull(manve.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
        } else if (exception instanceof BindException bindException && !bindException.getBindingResult().getFieldErrors().isEmpty()) {
            message = Objects.requireNonNull(bindException.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        var message = exception.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("validation failed");
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        var message = "invalid value for " + exception.getName();
        if (UUID.class.equals(exception.getRequiredType())) {
            message = exception.getName() + " must be a valid UUID";
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException exception) {
        var message = findCauseMessage(exception);
        if (message == null || message.isBlank()) {
            message = "request body is malformed";
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(DuplicateProjectNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateProjectNameException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ProjectNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
    }

    private String findCauseMessage(Throwable throwable) {
        var current = throwable;
        while (current != null) {
            if (current instanceof IllegalArgumentException illegalArgumentException && illegalArgumentException.getMessage() != null) {
                return illegalArgumentException.getMessage();
            }
            current = current.getCause();
        }
        return null;
    }
}
