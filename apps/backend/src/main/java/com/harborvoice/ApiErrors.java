package com.harborvoice;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrors {
    private final com.harborvoice.identity.SessionService sessions;

    public ApiErrors(com.harborvoice.identity.SessionService sessions) {
        this.sessions = sessions;
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    ResponseEntity<Void> deniedOrMissing(org.springframework.web.server.ResponseStatusException error,
            jakarta.servlet.http.HttpServletRequest request) {
        int status = error.getStatusCode().value();
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if ((status == 403 || status == 404) && authentication != null
                && authentication.getPrincipal() instanceof com.harborvoice.identity.Actor actor) {
            try {
                // Do not persist caller-controlled URLs, request bodies, or guessed foreign IDs.
                sessions.audit(actor, "ACCESS_DENIED", actor.employeeId(), "DENIED",
                        (java.util.UUID) request.getAttribute("correlationId"));
            } catch (DataAccessException unavailable) {
                return ResponseEntity.status(503).build();
            }
        }
        return ResponseEntity.status(status).build();
    }

    @ExceptionHandler({org.springframework.web.bind.MethodArgumentNotValidException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class})
    ResponseEntity<Void> invalidRequest(Exception error) {
        // Do not log validation objects; they may contain passwords or other personal data.
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    ResponseEntity<Void> duplicateInput(Exception error) {
        return ResponseEntity.status(409).build();
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<Void> databaseUnavailable(DataAccessException error) {
        return ResponseEntity.status(503).build();
    }
}
