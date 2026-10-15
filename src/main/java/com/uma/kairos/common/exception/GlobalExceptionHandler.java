package com.umasankar.applytrack.common.exception;

import com.umasankar.applytrack.common.api.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() == null ? status.getReasonPhrase() : exception.getReason();
        String code = switch (status) {
            case CONFLICT -> "CONFLICT";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE";
            case BAD_GATEWAY -> "UPSTREAM_FAILURE";
            default -> "REQUEST_FAILED";
        };
        return response(status, code, message, request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fields = exception.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(FieldError::getField, error -> error.getDefaultMessage(), (first, ignored) -> first));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "One or more fields are invalid", request, fields);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        String message = "Parameter '%s' has an invalid value".formatted(exception.getName());
        return response(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", message, request, Map.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                              HttpServletRequest request, Map<String, String> fieldErrors) {
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), code,
            message, request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
