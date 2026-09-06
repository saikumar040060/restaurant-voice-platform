package com.harborvoice.platform.action;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ActionDispatchWorkerTest {
    @Test
    void refusesTerminalOutboxJobsBeforeGateway() {
        UUID id = UUID.randomUUID(), business = UUID.randomUUID();
        var request = new ActionRequest(id, business, UUID.randomUUID(), "fixture.lookup", "worker-key", Map.of());
        var permit = new PolicyGateway.ActionPermit(id, business, "fixture.lookup", "a".repeat(64),
                new PolicyGateway.InstantExpiry(Instant.now().plusSeconds(30)));
        var worker = new ActionDispatchWorker(new ReplaySafeActionExecutor((p, r) ->
                new ActionGateway.ActionResult("FIXTURE_ACCEPTED", "ref")));
        assertThrows(IllegalArgumentException.class, () -> worker.dispatch(business, OutboxStatus.DEAD_LETTER, permit, request));
        assertThrows(IllegalArgumentException.class, () -> worker.dispatch(UUID.randomUUID(), OutboxStatus.PENDING, permit, request));
    }
}
