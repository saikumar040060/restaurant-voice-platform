package com.harborvoice.platform.knowledge;

import java.time.Instant;
import java.util.UUID;

public record KnowledgeRecord(UUID id, UUID businessId, UUID locationId, String key, String content,
                              String provenance, int version, ApprovalState approvalState, Instant freshUntil) {
    public enum ApprovalState { DRAFT, APPROVED, REVOKED }
}
