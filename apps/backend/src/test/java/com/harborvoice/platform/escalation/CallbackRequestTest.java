package com.harborvoice.platform.escalation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CallbackRequestTest {

    @Test
    void trimsAndBoundsDestination() {
        var business = UUID.randomUUID();
        var conversation = UUID.randomUUID();
        assertEquals("+15551234567", new CallbackRequest(business, conversation, " +15551234567 ",
                Instant.now(), true).destination());
        assertThrows(IllegalArgumentException.class, () -> new CallbackRequest(business, conversation, "x".repeat(321), Instant.now(), true));
    }
    @Test void rejectsCallbackWithoutExplicitConsent() {
        assertThatThrownBy(() -> new CallbackRequest(UUID.randomUUID(), UUID.randomUUID(), "+15551234567",
                Instant.now(), false)).isInstanceOf(IllegalArgumentException.class);
    }
}
