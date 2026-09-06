package com.harborvoice.platform.action;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActionRequestTest {
    @Test
    void rejectsOversizedArgumentMaps() {
        var args = new java.util.HashMap<String, Object>();
        for (int i = 0; i < 101; i++) args.put("arg-" + i, i);
        assertThatThrownBy(() -> new ActionRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "restaurant.quote", "key", args)).isInstanceOf(IllegalArgumentException.class);
    }
}
