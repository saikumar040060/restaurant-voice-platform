package com.harborvoice.platform.escalation;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FixtureEscalationPortTest {
    @Test
    void storesBeforeReturningAndEnforcesBusinessScope() {
        UUID business = UUID.randomUUID();
        var port = new FixtureEscalationPort();
        var item = port.request(business, UUID.randomUUID(), EscalationCase.Reason.POLICY_BLOCK);
        assertEquals(EscalationCase.State.REQUESTED, item.state());
        assertEquals(EscalationCase.State.CALLBACK_PENDING,
                port.transition(business, item.id(), EscalationCase.State.CALLBACK_PENDING).state());
        assertThrows(IllegalArgumentException.class, () -> port.transition(UUID.randomUUID(), item.id(), EscalationCase.State.RESOLVED));
    }
}
