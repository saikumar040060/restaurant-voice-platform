package com.harborvoice.modules.restaurant;

import java.util.List;

public final class OrderPricing {
    private OrderPricing() { }

    public record Line(MenuItem item, String modifier, int quantity) { }
    public record Quote(List<Line> lines, int subtotalMinor, String currency) {
        public Quote { lines = List.copyOf(lines); }
    }

    public static Quote quote(List<Line> lines, String currency) {
        if (lines == null || lines.isEmpty() || currency == null || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("order and currency required");
        }
        int total = 0;
        for (Line line : lines) {
            if (line == null || line.item() == null || line.quantity() < 1 || line.quantity() > 99) {
                throw new IllegalArgumentException("invalid order line");
            }
            int modifier = line.modifier() == null ? 0 : line.item().modifiers().getOrDefault(line.modifier(), -1);
            if (modifier < 0) throw new IllegalArgumentException("unknown modifier");
            total = Math.addExact(total, Math.multiplyExact(line.quantity(), line.item().priceMinor() + modifier));
        }
        return new Quote(lines, total, currency);
    }
}
