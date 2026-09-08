package com.harborvoice.modules.restaurant;

import java.time.Instant;
import java.util.UUID;

public record MenuReviewDecision(UUID businessId, int itemIndex, Decision decision, String correction, String rationale,
                                 int version, String publicationState, String draftRevision, UUID actorId, Instant decidedAt) {
    public enum Decision { APPROVED, CORRECTED, REJECTED }
    public MenuReviewDecision {
        if (businessId == null || itemIndex < 1 || decision == null || version < 1 || actorId == null || decidedAt == null
                || !"UNPUBLISHED".equals(publicationState) || draftRevision == null || !draftRevision.matches("[0-9a-f]{64}")
                || (decision == Decision.CORRECTED) != (correction != null && !correction.isBlank())
                || (decision == Decision.REJECTED) != (rationale != null && !rationale.isBlank())) {
            throw new IllegalArgumentException("invalid unpublished menu review decision");
        }
    }
}
