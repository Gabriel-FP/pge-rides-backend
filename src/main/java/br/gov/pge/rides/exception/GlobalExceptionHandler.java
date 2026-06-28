package br.gov.pge.rides.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Triggered when @Valid fails (missing/blank/invalid fields)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        ApiError body = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    // Business rule: a user cannot have more than one active ride at a time
    @ExceptionHandler(ActiveRideAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleActiveRide(ActiveRideAlreadyExistsException ex) {
        return conflict(ex.getMessage());
    }

    // A driver tried to accept a ride that is no longer WAITING
    @ExceptionHandler(RideNotAvailableException.class)
    public ResponseEntity<ApiError> handleNotAvailable(RideNotAvailableException ex) {
        return conflict(ex.getMessage());
    }

    // Ride id does not exist (or is not in the cache)
    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(RideNotFoundException ex) {
        return notFound(ex.getMessage());
    }

    // Unknown route: keep Spring's 404 instead of letting the generic handler turn it into 500
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex) {
        return notFound("Resource not found");
    }

    // Safety net for anything unexpected: log it, but never leak internals to the client
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        ApiError body = new ApiError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ResponseEntity<ApiError> conflict(String message) {
        ApiError body = new ApiError(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                message,
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    private ResponseEntity<ApiError> notFound(String message) {
        ApiError body = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                message,
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
