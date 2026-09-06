package com.harborvoice.platform.action;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OutboxStatusTest {
    @Test
    void unknownRemainsReconciliationEligibleButDeadLetterIsTerminal() {
        assertFalse(OutboxStatus.UNKNOWN.terminal());
        assertTrue(OutboxStatus.DEAD_LETTER.terminal());
        assertTrue(OutboxStatus.RECONCILED.terminal());
        assertTrue(OutboxStatus.PENDING.retryable());
        assertFalse(OutboxStatus.UNKNOWN.retryable());
    }
}
