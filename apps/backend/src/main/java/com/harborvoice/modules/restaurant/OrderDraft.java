package com.harborvoice.modules.restaurant;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record OrderDraft(UUID id, List<OrderPricing.Line> lines, OrderPricing.Quote quote, boolean confirmed,
                         String availabilityRevision) {
    public OrderDraft {
        Objects.requireNonNull(id); lines = List.copyOf(lines); Objects.requireNonNull(quote);
    }

    public static OrderDraft create(UUID id, List<OrderPricing.Line> lines, String currency) {
        return new OrderDraft(id, lines, OrderPricing.quote(lines, currency), false, null);
    }

    public static OrderDraft create(UUID id, List<OrderPricing.Line> lines, String currency, AvailabilitySnapshot snapshot) {
        if (snapshot == null || lines == null || lines.stream().anyMatch(line -> line == null || !snapshot.available(line.item().sku()))) {
            throw new IllegalArgumentException("current availability snapshot required");
        }
        return new OrderDraft(id, lines, OrderPricing.quote(lines, currency), false, snapshot.revision());
    }

    public OrderDraft confirm(OrderPricing.Quote currentQuote) {
        if (currentQuote == null || currentQuote.subtotalMinor() != quote.subtotalMinor()
                || !currentQuote.lines().equals(quote.lines()) || !currentQuote.currency().equals(quote.currency())) {
            throw new IllegalArgumentException("quote changed; confirmation required again");
        }
        return new OrderDraft(id, lines, quote, true, availabilityRevision);
    }

    public OrderDraft confirm(OrderPricing.Quote currentQuote, AvailabilitySnapshot snapshot) {
        if (availabilityRevision == null || snapshot == null || !availabilityRevision.equals(snapshot.revision())
                || lines.stream().anyMatch(line -> !snapshot.available(line.item().sku()))) {
            throw new IllegalArgumentException("availability changed; confirmation required again");
        }
        return confirm(currentQuote);
    }
}
