package com.harborvoice;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler({org.springframework.web.bind.MethodArgumentNotValidException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class})
    ResponseEntity<Void> invalidRequest(Exception error) {
        // Do not log validation objects; they may contain passwords or other personal data.
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<Void> databaseUnavailable(DataAccessException error) {
        return ResponseEntity.status(503).build();
    }
}
