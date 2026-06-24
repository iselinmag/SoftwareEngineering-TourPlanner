package com.tourplanner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

// this is the one place that catches our custom errors and turns them into
// clean http responses with the right status code and a simple json message.
// @RestControllerAdvice means it watches every controller in the app.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // someone asked for a thing that does not exist -> 404 not found
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // someone tried to touch a thing that is not theirs -> 403 forbidden
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    // someone sent data that breaks our field rules -> 400 bad request
    // we collect each broken field's message so the frontend can show them
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, String> errors = new java.util.HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(errors);
    }

    // someone tried to upload a file bigger than the allowed limit -> 413 payload too large
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleTooLarge(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("error", "That image is too large. Please choose a smaller file."));
    }
}
