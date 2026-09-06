package com.harborvoice.platform.audit;

import java.time.Instant;
import java.util.UUID;

public record ConsentRecord(UUID id, UUID businessId, UUID conversationId, ConsentPurpose purpose,
                            boolean granted, String evidenceHash, Instant recordedAt) {
    public ConsentRecord {
        if (id == null || businessId == null || purpose == null || evidenceHash == null || evidenceHash.isBlank()
                || recordedAt == null) throw new IllegalArgumentException("invalid consent record");
    }
}
