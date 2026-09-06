package com.harborvoice.platform.conversation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConversationTurnTest {
    @Test void rejectsUnknownSpeakerRoles() {
        assertThatThrownBy(() -> new ConversationTurn(UUID.randomUUID(), UUID.randomUUID(), 0, 0,
                "MODEL", "text", Instant.now(), true)).isInstanceOf(IllegalArgumentException.class);
    }
}
