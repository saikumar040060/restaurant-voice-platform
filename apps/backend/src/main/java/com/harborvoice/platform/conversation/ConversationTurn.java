package com.harborvoice.platform.conversation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ConversationTurn(UUID turnId, UUID conversationId, long sequence, long epoch,
                               String speaker, String text, Instant occurredAt, boolean finalText) {
    public ConversationTurn {
        Objects.requireNonNull(turnId, "turnId");
        Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(speaker, "speaker");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(occurredAt, "occurredAt");
        if (sequence < 0 || epoch < 0 || text.length() > 16_000
                || !(speaker.equals("CUSTOMER") || speaker.equals("AGENT") || speaker.equals("SYSTEM"))) {
            throw new IllegalArgumentException("invalid conversation turn");
        }
    }
}
