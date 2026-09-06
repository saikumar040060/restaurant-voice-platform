package com.harborvoice.platform.action;

import java.util.UUID;

/** Server-side policy boundary. Model output is never executable by itself. */
public interface PolicyGateway {
    ActionPermit authorize(ActionRequest request, ConfirmationEvidence confirmation);

    record ActionPermit(UUID requestId, UUID businessId, String toolId, String requestHash,
                        InstantExpiry expiresAt) {
        public ActionPermit {
            if (requestId == null || businessId == null || toolId == null || toolId.isBlank() || requestHash == null
                    || !requestHash.matches("[A-Fa-f0-9]{64}") || expiresAt == null) {
                throw new IllegalArgumentException("invalid action permit");
            }
        }
    }

    record InstantExpiry(java.time.Instant value) {
        public InstantExpiry {
            if (value == null) throw new IllegalArgumentException("expiry required");
        }
    }
}
