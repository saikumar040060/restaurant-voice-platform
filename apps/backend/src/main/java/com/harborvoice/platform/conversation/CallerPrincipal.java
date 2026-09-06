package com.harborvoice.platform.conversation;

import java.util.UUID;

/** Unauthenticated caller context; caller hints never confer employee authority. */
public record CallerPrincipal(UUID businessId, UUID conversationId, String callerHint) {
    public CallerPrincipal {
        if (businessId == null || conversationId == null || callerHint == null || callerHint.length() > 320) {
            throw new IllegalArgumentException("invalid caller principal");
        }
        callerHint = callerHint.trim();
    }
}
