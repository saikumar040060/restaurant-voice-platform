package com.harborvoice.platform.action;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class ReplaySafeActionExecutorTest {
    @Test
    void invokesGatewayOnceForReplayedRequest() {
        UUID id = UUID.randomUUID(), business = UUID.randomUUID();
        var request = new ActionRequest(id, business, UUID.randomUUID(), "fixture.lookup", "key", Map.of());
        var permit = new PolicyGateway.ActionPermit(id, business, "fixture.lookup", "a".repeat(64),
                new PolicyGateway.InstantExpiry(Instant.now().plusSeconds(30)));
        AtomicInteger calls = new AtomicInteger();
        var executor = new ReplaySafeActionExecutor((p, r) -> {
            calls.incrementAndGet(); return new ActionGateway.ActionResult("FIXTURE_ACCEPTED", "ref");
        });
        assertSame(executor.execute(permit, request), executor.execute(permit, request));
        assertEquals(1, calls.get());
    }

    @Test
    void preservesUnknownOutcomeAcrossReplay() {
        UUID id = UUID.randomUUID(), business = UUID.randomUUID();
        var request = new ActionRequest(id, business, UUID.randomUUID(), "fixture.lookup", "key-unknown", Map.of());
        var permit = new PolicyGateway.ActionPermit(id, business, "fixture.lookup", "b".repeat(64),
                new PolicyGateway.InstantExpiry(Instant.now().plusSeconds(30)));
        AtomicInteger calls = new AtomicInteger();
        var executor = new ReplaySafeActionExecutor((p, r) -> {
            calls.incrementAndGet(); return new ActionGateway.ActionResult("UNKNOWN", "uncertain");
        });
        assertEquals("UNKNOWN", executor.execute(permit, request).status());
        assertEquals("UNKNOWN", executor.execute(permit, request).status());
        assertEquals(1, calls.get());
    }
}
