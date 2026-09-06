package com.harborvoice.platform.audit;

import java.time.Instant;
import java.util.UUID;

public record PlatformAuditEvent(UUID id, UUID businessId, UUID actorId, String action,
                                UUID targetId, String outcome, UUID correlationId, Instant occurredAt) {
    public PlatformAuditEvent {
        if (id == null || businessId == null || action == null || action.isBlank() || targetId == null
                || outcome == null || outcome.isBlank() || correlationId == null || occurredAt == null) {
            throw new IllegalArgumentException("invalid platform audit event");
        }
    }
}
