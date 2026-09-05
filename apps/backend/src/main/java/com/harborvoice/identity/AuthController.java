package com.harborvoice.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final SessionService sessions;

    private final LoginLimiter limiter;

    public AuthController(SessionService sessions, LoginLimiter limiter) {
        this.sessions = sessions;
        this.limiter = limiter;
    }

    public record Login(@NotBlank @Size(max = 120) String username,
                        @NotBlank @Size(max = 72) String password) { }

    @PostMapping("/api/v1/auth/login")
    ResponseEntity<?> login(@Valid @RequestBody Login login, HttpServletRequest request) {
        if (login.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest().build();
        }
        if (!limiter.allow(login.username(), request.getRemoteAddr())) {
            return ResponseEntity.status(429).header("Retry-After", "300").build();
        }
        return sessions.login(login.username(), login.password(), (UUID) request.getAttribute("correlationId"))
                .<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(401).build());
    }

    @GetMapping("/api/v1/me")
    Actor me(@AuthenticationPrincipal Actor actor) {
        return actor;
    }

    @PostMapping("/api/v1/auth/logout")
    ResponseEntity<Void> logout(@AuthenticationPrincipal Actor actor, HttpServletRequest request) {
        sessions.revoke(actor, request.getHeader("Authorization").substring(7),
                (UUID) request.getAttribute("correlationId"));
        return ResponseEntity.noContent().build();
    }
}
