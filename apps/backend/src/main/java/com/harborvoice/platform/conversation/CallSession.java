package com.harborvoice.platform.conversation;

import java.time.Instant;
import java.util.UUID;

public record CallSession(UUID id, UUID businessId, String channel, ConversationState state,
                          Instant startedAt, Instant endedAt) {
    public CallSession {
        if (id == null || businessId == null || channel == null || channel.isBlank()
                || state == null || startedAt == null) throw new IllegalArgumentException("invalid call session");
        if (endedAt != null && endedAt.isBefore(startedAt)) throw new IllegalArgumentException("invalid end time");
    }
}
