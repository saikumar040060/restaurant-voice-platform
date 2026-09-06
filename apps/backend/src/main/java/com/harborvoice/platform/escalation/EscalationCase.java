package com.harborvoice.platform.escalation;

import java.time.Instant;
import java.util.UUID;

public record EscalationCase(UUID id, UUID businessId, UUID conversationId, Reason reason,
                             State state, Instant createdAt) {
    public EscalationCase {
        if (id == null || businessId == null || conversationId == null || reason == null || state == null || createdAt == null) {
            throw new IllegalArgumentException("escalation scope and state required");
        }
    }
    public enum Reason { CUSTOMER_REQUEST, POLICY_BLOCK, UNKNOWN_ACTION, SYSTEM_FAILURE }
    public enum State { REQUESTED, TRANSFER_PENDING, CALLBACK_PENDING, RESOLVED, DECLINED }

    public boolean canTransitionTo(State next) {
        if (next == null || state == State.RESOLVED || state == State.DECLINED) return false;
        return switch (state) {
            case REQUESTED -> next == State.TRANSFER_PENDING || next == State.CALLBACK_PENDING || next == State.DECLINED;
            case TRANSFER_PENDING, CALLBACK_PENDING -> next == State.RESOLVED || next == State.DECLINED;
            case RESOLVED, DECLINED -> false;
        };
    }
}
