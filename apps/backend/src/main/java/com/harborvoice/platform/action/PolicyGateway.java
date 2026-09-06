package com.harborvoice.platform.action;

import java.util.UUID;

/** Server-side policy boundary. Model output is never executable by itself. */
public interface PolicyGateway {
    ActionPermit authorize(ActionRequest request, ConfirmationEvidence confirmation);

    record ActionPermit(UUID requestId, UUID businessId, String toolId, String requestHash,
                        InstantExpiry expiresAt) {
        public ActionPermit {
            if (requestId == null || businessId == null || toolId == null || requestHash == null || expiresAt == null) {
                throw new NullPointerException("permit fields are required");
            }
        }
    }

    record InstantExpiry(java.time.Instant value) { }
}
