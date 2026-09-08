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

    /** Redaction-safe fixture queue for authorized operations views. */
    public java.util.List<EscalationCase> recent(UUID businessId, int limit) {
        if (businessId == null || limit < 1 || limit > 100) throw new IllegalArgumentException("invalid queue scope");
        return cases.values().stream().filter(item -> businessId.equals(item.businessId()))
                .sorted(java.util.Comparator.comparing(EscalationCase::createdAt).reversed())
                .limit(limit).toList();
    }
}
