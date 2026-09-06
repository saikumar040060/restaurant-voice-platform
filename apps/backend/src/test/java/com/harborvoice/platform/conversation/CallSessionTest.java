package com.harborvoice.platform.conversation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CallSessionTest {
    @Test void rejectsEndBeforeStart() {
        Instant now = Instant.now();
        assertThatThrownBy(() -> new CallSession(UUID.randomUUID(), UUID.randomUUID(), "phone",
                ConversationState.ACTIVE, now, now.minusSeconds(1))).isInstanceOf(IllegalArgumentException.class);
    }
}
