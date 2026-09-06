package com.harborvoice.platform.action;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FixtureActionGatewayTest {
    @Test void acceptsOnlyMatchingUnexpiredPermit() {
        UUID id = UUID.randomUUID(), business = UUID.randomUUID(), conversation = UUID.randomUUID();
        var request = new ActionRequest(id, business, conversation, "fixture.lookup", "key-1", Map.of());
        var permit = new PolicyGateway.ActionPermit(id, business, "fixture.lookup", "hash",
                new PolicyGateway.InstantExpiry(Instant.now().plusSeconds(30)));
        assertThat(new FixtureActionGateway().execute(permit, request).status()).isEqualTo("FIXTURE_ACCEPTED");
    }
}
