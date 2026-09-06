package com.harborvoice.platform.action;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ConfirmationEvidence(UUID conversationId, long epoch, String summary,
                                   String utteranceHash, Instant observedAt) {
    public ConfirmationEvidence {
        Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(summary, "summary");
        Objects.requireNonNull(utteranceHash, "utteranceHash");
        Objects.requireNonNull(observedAt, "observedAt");
        if (epoch < 0 || summary.isBlank() || summary.length() > 2_000 || utteranceHash.isBlank()) {
            throw new IllegalArgumentException("invalid confirmation evidence");
        }
    }
}
