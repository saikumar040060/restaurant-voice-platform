package com.harborvoice.platform.dialogue;

import com.harborvoice.identity.Actor;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TextConsoleControllerTest {
    @Test
    void returnsTenantBoundFixtureResponse() {
        UUID tenant = UUID.randomUUID();
        TextConsoleController.Response response = new TextConsoleController()
                .respond(new Actor(UUID.randomUUID(), tenant, Actor.Role.OWNER), new TextConsoleController.Request("hours?"));
        assertEquals(tenant, response.tenantId());
        assertEquals("I do not have an approved answer for that.", response.responseText());
    }
}
