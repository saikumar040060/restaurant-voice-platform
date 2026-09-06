package com.harborvoice.platform.escalation;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EscalationCaseTest {
    @Test void modelsTenantScopedCallbackEscalation() {
        var item = new EscalationCase(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                EscalationCase.Reason.CUSTOMER_REQUEST, EscalationCase.State.CALLBACK_PENDING, Instant.now());
        assertThat(item.state()).isEqualTo(EscalationCase.State.CALLBACK_PENDING);
    }
}
