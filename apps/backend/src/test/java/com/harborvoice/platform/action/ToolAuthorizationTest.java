package com.harborvoice.platform.action;

import com.harborvoice.platform.module.ToolCapability;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ToolAuthorizationTest {
    @Test
    void readToolsStillRequireCapabilityMatch() {
        var request = new ActionRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "menu.lookup", "key", Map.of());
        var read = new ToolCapability("menu.lookup", false, false);
        ToolAuthorization.require(read, request, false);
        assertThrows(IllegalArgumentException.class, () -> ToolAuthorization.require(
                new ToolCapability("other.lookup", false, false), request, false));
    }

    @Test
    void mutatingToolsRequireExplicitConfirmation() {
        var request = new ActionRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "order.submit", "key-2", Map.of());
        var mutating = new ToolCapability("order.submit", true, true);
        assertThrows(IllegalArgumentException.class, () -> ToolAuthorization.require(mutating, request, false));
        ToolAuthorization.require(mutating, request, true);
    }
}
