package com.harborvoice.platform.conversation;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CallerPrincipalTest {
    @Test
    void callerHintIsBoundedAndNotAnEmployeeRole() {
        var caller = new CallerPrincipal(UUID.randomUUID(), UUID.randomUUID(), " +15551234567 ");
        assertEquals("+15551234567", caller.callerHint());
        assertThrows(IllegalArgumentException.class, () -> new CallerPrincipal(UUID.randomUUID(), UUID.randomUUID(), "x".repeat(321)));
    }
}
