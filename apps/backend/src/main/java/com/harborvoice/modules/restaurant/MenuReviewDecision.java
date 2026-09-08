package com.harborvoice.modules.restaurant;

import java.time.Instant;
import java.util.UUID;

public record MenuReviewDecision(UUID businessId, int itemIndex, Decision decision, String correction,
                                 int version, String publicationState, UUID actorId, Instant decidedAt) {
    public enum Decision { APPROVED, CORRECTED, REJECTED }
    public MenuReviewDecision {
        if (businessId == null || itemIndex < 1 || decision == null || version < 1 || actorId == null || decidedAt == null
                || !"UNPUBLISHED".equals(publicationState) || (decision == Decision.CORRECTED) != (correction != null && !correction.isBlank())) {
            throw new IllegalArgumentException("invalid unpublished menu review decision");
        }
    }
}
