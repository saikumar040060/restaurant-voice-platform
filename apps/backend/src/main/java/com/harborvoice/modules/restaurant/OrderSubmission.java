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
        try {
            return new OrderSubmission(draft.id(), draft.quote(), HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(canonicalQuote(draft.quote()).getBytes(StandardCharsets.UTF_8))));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String canonicalQuote(OrderPricing.Quote quote) {
        StringBuilder value = new StringBuilder("v1|").append(quote.currency()).append('|')
                .append(quote.subtotalMinor()).append('|');
        for (OrderPricing.Line line : quote.lines()) {
            String modifier = OrderPricing.canonicalModifiers(line.modifier());
            value.append(line.item().sku().length()).append(':').append(line.item().sku()).append('|')
                    .append(modifier.length()).append(':').append(modifier).append('|')
                    .append(line.quantity()).append('|').append(line.item().priceMinor()).append('|');
        }
        return value.toString();
    }
}
