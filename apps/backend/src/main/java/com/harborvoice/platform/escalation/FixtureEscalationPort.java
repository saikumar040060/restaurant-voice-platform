package com.harborvoice.platform.escalation;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory escalation adapter; success is returned only after the case is stored. */
public final class FixtureEscalationPort implements EscalationPort {
    private final ConcurrentHashMap<UUID, EscalationCase> cases = new ConcurrentHashMap<>();

    @Override
    public EscalationCase request(UUID businessId, UUID conversationId, EscalationCase.Reason reason) {
        var item = new EscalationCase(UUID.randomUUID(), businessId, conversationId, reason,
                EscalationCase.State.REQUESTED, Instant.now());
        cases.putIfAbsent(item.id(), item);
        return cases.get(item.id());
    }

    @Override
    public EscalationCase transition(UUID businessId, UUID caseId, EscalationCase.State next) {
        return cases.compute(caseId, (id, current) -> {
            if (current == null || !current.businessId().equals(businessId) || !current.canTransitionTo(next)) {
                throw new IllegalArgumentException("escalation transition denied");
            }
            return new EscalationCase(current.id(), current.businessId(), current.conversationId(), current.reason(), next, current.createdAt());
        });
    }
}
