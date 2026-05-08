package io.github.siniarski.viruni;

import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestResponse {

    public static class ErrorResponse {
        private final String message;
        private final Object detail;

        public ErrorResponse(String message) {
            this.message = message;
            this.detail = null;
        }

        public ErrorResponse(String message, Object detail) {
            this.message = message;
            this.detail = detail;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class ValidationError {
        private final String field;
        private final String message;

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }

    public static <T> ResponseEntity<T> ok() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> ok(T obj) {
        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> noContent() {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public static <T> ResponseEntity<T> created(T obj) {
        return new ResponseEntity<>(obj, HttpStatus.CREATED);
    }

    public static ResponseEntity<ErrorResponse> badRequest(String message) {
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity<ErrorResponse> badRequest(String message, Object detail) {
        return new ResponseEntity<>(new ErrorResponse(message, detail), HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity<Map<String, Object>> validationError(List<ValidationError> errors) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Form validation failed");
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    public static <T> ResponseEntity<Map<String, Object>> validationError(Set<ConstraintViolation<T>> violations) {
        List<RestResponse.ValidationError> errors = violations.stream()
                .map(c -> new RestResponse.ValidationError(c.getPropertyPath().toString(), c.getMessage()))
                .toList();

        return validationError(errors);
    }

    public static ResponseEntity<Map<String, Object>> validationErrorWildcard(Set<ConstraintViolation<?>> violations) {
        List<RestResponse.ValidationError> errors = violations.stream()
                .map(c -> new RestResponse.ValidationError(c.getPropertyPath().toString(), c.getMessage()))
                .toList();

        return validationError(errors);
    }

    public static ResponseEntity<ErrorResponse> notFound(String message) {
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.NOT_FOUND);
    }

    public static <T> ResponseEntity<T> notFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public static ResponseEntity<ErrorResponse> unauthorized(String message) {
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.UNAUTHORIZED);
    }

    public static ResponseEntity<ErrorResponse> unauthorized(String message, Object detail) {
        return new ResponseEntity<>(new ErrorResponse(message, detail), HttpStatus.UNAUTHORIZED);
    }

    public static ResponseEntity<ErrorResponse> conflict(String message) {
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.CONFLICT);
    }

    public static ResponseEntity<ErrorResponse> forbidden(String message) {
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.FORBIDDEN);
    }

    public static ResponseEntity<ErrorResponse> internalServerError(String message) {
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ResponseEntity<ErrorResponse> internalServerError() {
        return internalServerError("Internal Server Error");
    }
}
