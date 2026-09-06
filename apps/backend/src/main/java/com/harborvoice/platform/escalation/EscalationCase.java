package com.harborvoice.platform.escalation;

import java.time.Instant;
import java.util.UUID;

public record EscalationCase(UUID id, UUID businessId, UUID conversationId, Reason reason,
                             State state, Instant createdAt) {
    public enum Reason { CUSTOMER_REQUEST, POLICY_BLOCK, UNKNOWN_ACTION, SYSTEM_FAILURE }
    public enum State { REQUESTED, TRANSFER_PENDING, CALLBACK_PENDING, RESOLVED, DECLINED }
}
