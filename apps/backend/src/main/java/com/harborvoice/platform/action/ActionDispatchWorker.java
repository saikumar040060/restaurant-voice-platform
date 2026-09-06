package com.harborvoice.platform.action;

import java.util.UUID;

/** Provider-free worker guard for outbox jobs; adapters remain behind ActionGateway. */
public final class ActionDispatchWorker {
    private final ReplaySafeActionExecutor executor;

    public ActionDispatchWorker(ReplaySafeActionExecutor executor) {
        this.executor = java.util.Objects.requireNonNull(executor, "executor");
    }

    public ActionGateway.ActionResult dispatch(UUID businessId, OutboxStatus status,
                                               PolicyGateway.ActionPermit permit, ActionRequest request) {
        if (businessId == null || status == null || !status.retryable()) {
            throw new IllegalArgumentException("outbox job is not retryable");
        }
        if (!businessId.equals(request == null ? null : request.businessId())) {
            throw new IllegalArgumentException("outbox business scope mismatch");
        }
        return executor.execute(permit, request);
    }
}
