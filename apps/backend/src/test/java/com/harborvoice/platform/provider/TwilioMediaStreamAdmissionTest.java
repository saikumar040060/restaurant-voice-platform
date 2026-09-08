package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TwilioMediaStreamAdmissionTest {
    @Test void grantsOneScopedAllowlistedStreamAndRejectsReplay() {
        var gate = new SandboxSpendGate(new CallAdmissionController(new ProviderSafetyConfig(true, Set.of("+15550000000"), 1, 60, 100)));
        var admission = new TwilioMediaStreamAdmission(gate); var now = Instant.parse("2026-09-08T12:00:00Z");
        UUID business = UUID.randomUUID(), conversation = UUID.randomUUID();
        UUID token = admission.issue(business, conversation, "+15550000000", 1, now);
        var grant = admission.consume(token, now.plusSeconds(1));
        assertThat(grant.businessId()).isEqualTo(business); assertThat(grant.conversationId()).isEqualTo(conversation);
        assertThatThrownBy(() -> admission.consume(token, now.plusSeconds(2))).isInstanceOf(IllegalArgumentException.class);
        admission.close(grant);
    }
}
