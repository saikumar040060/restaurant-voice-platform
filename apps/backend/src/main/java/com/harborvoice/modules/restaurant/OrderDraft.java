package com.harborvoice.modules.restaurant;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record OrderDraft(UUID id, List<OrderPricing.Line> lines, OrderPricing.Quote quote, boolean confirmed) {
    public OrderDraft {
        Objects.requireNonNull(id); lines = List.copyOf(lines); Objects.requireNonNull(quote);
    }

    public static OrderDraft create(UUID id, List<OrderPricing.Line> lines, String currency) {
        return new OrderDraft(id, lines, OrderPricing.quote(lines, currency), false);
    }

    public OrderDraft confirm(OrderPricing.Quote currentQuote) {
        if (currentQuote == null || currentQuote.subtotalMinor() != quote.subtotalMinor()
                || !currentQuote.lines().equals(quote.lines()) || !currentQuote.currency().equals(quote.currency())) {
            throw new IllegalArgumentException("quote changed; confirmation required again");
        }
        return new OrderDraft(id, lines, quote, true);
    }
}
