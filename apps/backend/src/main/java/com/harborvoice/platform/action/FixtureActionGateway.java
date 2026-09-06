package com.harborvoice.platform.action;

import java.time.Instant;

/** Local adapter that records no external side effect; useful for policy contract tests. */
public final class FixtureActionGateway implements ActionGateway {
    @Override
    public ActionResult execute(PolicyGateway.ActionPermit permit, ActionRequest request) {
        if (!permit.requestId().equals(request.requestId()) || !permit.businessId().equals(request.businessId())
                || !permit.toolId().equals(request.toolId()) || permit.expiresAt().value().isBefore(Instant.now())) {
            throw new IllegalArgumentException("invalid or expired action permit");
        }
        return new ActionResult("FIXTURE_ACCEPTED", "fixture-" + request.requestId());
    }
}
