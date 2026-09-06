package com.harborvoice.modules.restaurant;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

public record OrderSubmission(UUID orderId, OrderPricing.Quote quote, String quoteHash) {
    public OrderSubmission {
        if (orderId == null || quote == null || quoteHash == null || quoteHash.length() != 64) {
            throw new IllegalArgumentException("invalid order submission");
        }
    }

    public static OrderSubmission from(OrderDraft draft) {
        if (draft == null || !draft.confirmed()) throw new IllegalArgumentException("order requires confirmation");
        String value = draft.quote().lines().toString() + ":" + draft.quote().subtotalMinor() + ":" + draft.quote().currency();
        try {
            return new OrderSubmission(draft.id(), draft.quote(), HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
