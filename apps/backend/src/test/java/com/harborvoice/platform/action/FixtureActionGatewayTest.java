package com.harborvoice.platform.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FixtureActionGatewayTest {
    @Test void acceptsOnlyMatchingUnexpiredPermit() {
        UUID id = UUID.randomUUID(), business = UUID.randomUUID(), conversation = UUID.randomUUID();
        var request = new ActionRequest(id, business, conversation, "fixture.lookup", "key-1", Map.of());
        var permit = new PolicyGateway.ActionPermit(id, business, "fixture.lookup", "a".repeat(64),
                new PolicyGateway.InstantExpiry(Instant.now().plusSeconds(30)));
        assertThat(new FixtureActionGateway().execute(permit, request).status()).isEqualTo("FIXTURE_ACCEPTED");
    }

    @Test void rejectsExpiredOrCrossBusinessPermit() {
        UUID id = UUID.randomUUID(), business = UUID.randomUUID(), conversation = UUID.randomUUID();
        var request = new ActionRequest(id, business, conversation, "fixture.lookup", "key-2", Map.of());
        var expired = new PolicyGateway.ActionPermit(id, UUID.randomUUID(), "fixture.lookup", "b".repeat(64),
                new PolicyGateway.InstantExpiry(Instant.now().minusSeconds(1)));
        assertThatThrownBy(() -> new FixtureActionGateway().execute(expired, request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
