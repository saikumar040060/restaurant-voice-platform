package com.harborvoice.platform.config;

import java.util.Map;
import java.util.UUID;

public record BusinessProfile(UUID businessId, int version, String locale, String timezone,
                              Map<String, Object> config, ApprovalState approvalState) {
    public enum ApprovalState { DRAFT, APPROVED, REVOKED }

    public BusinessProfile {
        if (businessId == null || version < 1 || locale == null || locale.isBlank()
                || timezone == null || timezone.isBlank() || approvalState == null) {
            throw new IllegalArgumentException("invalid business profile");
        }
        config = Map.copyOf(config == null ? Map.of() : config);
    }
}
