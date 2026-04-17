package com.example.loanapprovalprocess.api.error;

import com.example.loanapprovalprocess.exceptions.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException exception,
                                                                    HttpServletRequest request) {
        return buildResponse(
                exception.getStatus(),
                exception.getErrorCode(),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                         HttpServletRequest request) {
        List<ApiValidationError> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(fieldError -> new ApiValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed.",
                request.getRequestURI(),
                details
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception,
                                                                      HttpServletRequest request) {
        List<ApiValidationError> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ApiValidationError(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed.",
                request.getRequestURI(),
                details
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
                                                                         HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "Request body is malformed or contains unsupported values.",
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception,
                                                                         HttpServletRequest request) {
        log.error("Data integrity violation on path {}", request.getRequestURI(), exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "TECHNICAL_ERROR",
                "Unexpected technical error occurred.",
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception on path {}", request.getRequestURI(), exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "TECHNICAL_ERROR",
                "Unexpected technical error occurred.",
                request.getRequestURI(),
                List.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status,
                                                           String errorCode,
                                                           String message,
                                                           String path,
                                                           List<ApiValidationError> details) {
        ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                status.value(),
                errorCode,
                message,
                path,
                details
        );
        return ResponseEntity.status(status).body(response);
    }
}
