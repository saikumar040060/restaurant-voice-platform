package com.harborvoice.platform.conversation;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ConversationContextTest {
    @Test
    void keepsNewestTurnsInOriginalOrderWithinBounds() {
        UUID conversation = UUID.randomUUID();
        List<ConversationTurn> turns = List.of(
                turn(conversation, 0, "one"), turn(conversation, 1, "two"), turn(conversation, 2, "three"));
        ConversationContext context = ConversationContext.bounded(turns, 2, 8);
        assertEquals(List.of("two", "three"), context.turns().stream().map(ConversationTurn::text).toList());
        assertEquals(8, context.characterCount());
    }

    private ConversationTurn turn(UUID conversation, long sequence, String text) {
        return new ConversationTurn(UUID.randomUUID(), conversation, sequence, 0, "CUSTOMER", text, Instant.now(), true);
    }
}
