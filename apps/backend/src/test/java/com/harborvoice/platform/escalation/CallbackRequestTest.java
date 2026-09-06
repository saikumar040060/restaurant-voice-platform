package com.harborvoice.platform.escalation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CallbackRequestTest {
    @Test void rejectsCallbackWithoutExplicitConsent() {
        assertThatThrownBy(() -> new CallbackRequest(UUID.randomUUID(), UUID.randomUUID(), "+15551234567",
                Instant.now(), false)).isInstanceOf(IllegalArgumentException.class);
    }
}
