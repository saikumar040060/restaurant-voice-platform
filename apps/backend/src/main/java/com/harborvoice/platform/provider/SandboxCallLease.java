package com.harborvoice.platform.provider;

import java.time.Instant;
import java.util.UUID;

/** Opaque, bounded sandbox admission lease; it contains no caller number or provider secret. */
public record SandboxCallLease(UUID id, Instant expiresAt) {
    public SandboxCallLease {
        if (id == null || expiresAt == null) throw new IllegalArgumentException("sandbox lease required");
    }
}
