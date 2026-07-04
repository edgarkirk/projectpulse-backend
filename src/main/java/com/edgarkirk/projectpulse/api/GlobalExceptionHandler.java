package com.edgarkirk.projectpulse.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.edgarkirk.projectpulse.api.dto.response.ErrorResponse;
import com.edgarkirk.projectpulse.service.DuplicateProjectNameException;
import com.edgarkirk.projectpulse.service.ProjectNotFoundException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred";
    private static final String DUPLICATE_PROJECT_MESSAGE = "Project name is already taken";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(firstValidationMessage(exception.getBindingResult())));
    }

    @ExceptionHandler(BindException.class)
    ResponseEntity<ErrorResponse> handleBindException(BindException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(firstValidationMessage(exception.getBindingResult())));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream().findFirst().map(jakarta.validation.ConstraintViolation::getMessage)
                .orElse("Request validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        log.warn("Invalid request body", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Request body is invalid"));
    }

    @ExceptionHandler({DuplicateProjectNameException.class, DataIntegrityViolationException.class})
    ResponseEntity<ErrorResponse> handleDuplicateProjectName(Exception exception) {
        log.warn("Duplicate project name rejected", exception);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(DUPLICATE_PROJECT_MESSAGE));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    ResponseEntity<ErrorResponse> handleProjectNotFound(ProjectNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Invalid request parameter"));
    }


    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(UNEXPECTED_ERROR_MESSAGE));
    }

    private static String firstValidationMessage(org.springframework.validation.BindingResult bindingResult) {
        if (bindingResult.getFieldError() != null) {
            return bindingResult.getFieldError().getDefaultMessage();
        }
        return bindingResult.getAllErrors().stream()
                .findFirst()
                .map(org.springframework.validation.ObjectError::getDefaultMessage)
                .orElse("Request validation failed");
    }
}
