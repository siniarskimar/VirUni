package io.github.siniarski.viruni;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<RestResponse.ErrorResponse> handleRuntimeException(RuntimeException ex) {
        return RestResponse.internalServerError(ex.getMessage());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<RestResponse.ErrorResponse> handleAuthorizationException(AuthorizationDeniedException ex) {
        return RestResponse.forbidden(ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RestResponse.ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        return RestResponse.unauthorized(ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        return RestResponse.validationErrorWildcard(ex.getConstraintViolations());
    }
}
