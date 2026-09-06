package com.harborvoice.identity;

import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class PasswordRules {
    private PasswordRules() { }

    public static void validate(String value) {
        if (value == null || value.isBlank() || value.length() < 12
                || value.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
