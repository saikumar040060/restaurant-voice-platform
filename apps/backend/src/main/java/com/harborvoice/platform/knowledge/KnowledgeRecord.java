package com.harborvoice.platform.knowledge;

import java.time.Instant;
import java.util.UUID;

public record KnowledgeRecord(UUID id, UUID businessId, UUID locationId, String key, String content,
                              String provenance, int version, ApprovalState approvalState, Instant freshUntil) {
    public enum ApprovalState { DRAFT, APPROVED, REVOKED }

    public KnowledgeRecord {
        if (id == null || businessId == null || key == null || key.isBlank() || content == null || content.isBlank()
                || provenance == null || provenance.isBlank() || version < 1 || approvalState == null) {
            throw new IllegalArgumentException("invalid knowledge record");
        }
    }
}
