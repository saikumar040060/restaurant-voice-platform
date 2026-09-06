package com.harborvoice.platform.escalation;

import java.time.Instant;
import java.util.UUID;

public record CallbackRequest(UUID businessId, UUID conversationId, String destination,
                             Instant requestedAt, boolean consentGranted) {
    public CallbackRequest {
        if (businessId == null || conversationId == null || destination == null || destination.isBlank()
                || destination.length() > 320
                || requestedAt == null || !consentGranted) {
            throw new IllegalArgumentException("explicit callback consent and fields are required");
        }
        destination = destination.trim();
    }
}
