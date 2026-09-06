package com.harborvoice.platform.action;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PolicyPermitTest {
    @Test
    void rejectsMalformedPermitHash() {
        assertThrows(IllegalArgumentException.class, () -> new PolicyGateway.ActionPermit(UUID.randomUUID(), UUID.randomUUID(),
                "restaurant.quote", "bad", new PolicyGateway.InstantExpiry(Instant.now())));
    }
}
