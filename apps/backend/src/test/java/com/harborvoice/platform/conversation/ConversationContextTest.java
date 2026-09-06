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

    @Test
    void rejectsNullSource() {
        assertThrows(NullPointerException.class, () -> ConversationContext.bounded(null, 1, 10));
    }

    @Test
    void rejectsMismatchedDeclaredCharacterCount() {
        UUID conversation = UUID.randomUUID();
        var turn = turn(conversation, 0, "hello");
        assertThrows(IllegalArgumentException.class, () -> new ConversationContext(List.of(turn), 4));
    }

    @Test
    void rejectsContextOverMaximum() {
        UUID conversation = UUID.randomUUID();
        var first = turn(conversation, 0, "x".repeat(10_000));
        var second = turn(conversation, 1, "x".repeat(10_000));
        assertThrows(IllegalArgumentException.class, () -> new ConversationContext(List.of(first, second), 20_000));
    }

    private ConversationTurn turn(UUID conversation, long sequence, String text) {
        return new ConversationTurn(UUID.randomUUID(), conversation, sequence, 0, "CUSTOMER", text, Instant.now(), true);
    }
}
