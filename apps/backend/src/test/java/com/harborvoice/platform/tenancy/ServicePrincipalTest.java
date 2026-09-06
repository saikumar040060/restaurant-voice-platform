package com.harborvoice.platform.tenancy;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ServicePrincipalTest {
    @Test
    void validatesScopedNonHumanIdentity() {
        assertEquals("outbox.worker", new ServicePrincipal(UUID.randomUUID(), UUID.randomUUID(), "outbox.worker").service());
        assertThrows(IllegalArgumentException.class, () -> new ServicePrincipal(UUID.randomUUID(), UUID.randomUUID(), "Employee"));
    }
}
