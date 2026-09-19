package com.fooddelivery.orderservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Business logic errors thrown via RuntimeException across services ───
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        HttpStatus status = resolveStatus(ex.getMessage());
        return ResponseEntity.status(status).body(buildBody(status, ex.getMessage()));
    }

    // ─── @Valid request body validation failures (e.g. UpdateOrderAddressRequest) ───
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildBody(HttpStatus.BAD_REQUEST, message));
    }

    // ─── Constraint violations (e.g. @PathVariable / @RequestParam validation) ───
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildBody(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    // ─── Catch-all fallback ───
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again."));
    }

    // Maps known message phrases to proper HTTP status codes instead of
    // defaulting everything to 500 — keeps frontend err.error.message toasts accurate.
    private HttpStatus resolveStatus(String message) {
        if (message == null) return HttpStatus.BAD_REQUEST;

        String lower = message.toLowerCase();

        if (lower.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }

        if (lower.contains("access denied")) {
            return HttpStatus.FORBIDDEN;
        }

        return HttpStatus.BAD_REQUEST;
    }

    private Map<String, Object> buildBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}