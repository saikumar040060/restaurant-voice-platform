package com.harborvoice.modules.restaurant;

import java.time.Instant;
import java.util.UUID;

public record MenuReviewCompletion(UUID businessId, String draftRevision, String decisionSetHash,
                                   String publicationState, UUID completedBy, Instant completedAt) {
    public MenuReviewCompletion {
        if (businessId == null || draftRevision == null || !draftRevision.matches("[0-9a-f]{64}")
                || decisionSetHash == null || !decisionSetHash.matches("[0-9a-f]{64}")
                || !"UNPUBLISHED".equals(publicationState) || completedBy == null || completedAt == null) {
            throw new IllegalArgumentException("invalid unpublished menu review completion");
        }
    }
}
