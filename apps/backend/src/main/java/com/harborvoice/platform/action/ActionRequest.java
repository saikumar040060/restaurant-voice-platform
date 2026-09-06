package com.harborvoice.platform.action;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record ActionRequest(UUID requestId, UUID businessId, UUID conversationId,
                           String toolId, String idempotencyKey, Map<String, Object> arguments) {
    public ActionRequest {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(businessId, "businessId");
        Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(toolId, "toolId");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        arguments = Map.copyOf(Objects.requireNonNull(arguments, "arguments"));
        if (toolId.isBlank() || idempotencyKey.isBlank() || idempotencyKey.length() > 128) {
            throw new IllegalArgumentException("invalid action request");
        }
    }
}
