package com.harborvoice.platform.action;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Provider-free exactly-once fixture: one request ID gets at most one gateway invocation. */
public final class ReplaySafeActionExecutor {
    private final ActionGateway gateway;
    private final ConcurrentHashMap<UUID, ActionGateway.ActionResult> outcomes = new ConcurrentHashMap<>();

    public ReplaySafeActionExecutor(ActionGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway, "gateway");
    }

    public ActionGateway.ActionResult execute(PolicyGateway.ActionPermit permit, ActionRequest request) {
        if (permit == null || request == null || !permit.requestId().equals(request.requestId())
                || !permit.businessId().equals(request.businessId()) || permit.expiresAt().value().isBefore(Instant.now())) {
            throw new IllegalArgumentException("invalid action execution scope");
        }
        return outcomes.computeIfAbsent(request.requestId(), ignored -> gateway.execute(permit, request));
    }
}
