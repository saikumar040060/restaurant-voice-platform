package com.harborvoice.platform.conversation;

import java.util.UUID;

/** Provider-neutral conversation state boundary; implementations must enforce tenant scope. */
public interface ConversationPort {
    ConversationState state(UUID conversationId, UUID businessId);

    void acceptTurn(ConversationTurn turn, UUID businessId);

    void interrupt(UUID conversationId, UUID businessId, long nextEpoch);
}
