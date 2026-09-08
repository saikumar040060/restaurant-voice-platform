package com.harborvoice.modules.restaurant;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

public final class OrderPricing {
    private OrderPricing() { }

    public record Line(MenuItem item, String modifier, int quantity) { }
    public record Quote(List<Line> lines, int subtotalMinor, String currency) {
        public Quote { lines = List.copyOf(lines); }
    }

    public static Quote quote(List<Line> lines, String currency) {
        if (lines == null || lines.isEmpty() || lines.size() > 100 || currency == null || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("order and currency required");
        }
        int total = 0;
        for (Line line : lines) {
            if (line == null || line.item() == null || line.quantity() < 1 || line.quantity() > 99) {
                throw new IllegalArgumentException("invalid order line");
            }
            int modifier = modifierPrice(line.item(), line.modifier());
            total = Math.addExact(total, Math.multiplyExact(line.quantity(), line.item().priceMinor() + modifier));
        }
        return new Quote(lines, total, currency);
    }

    /** Parses at most one option from each named group, joined by {@code |}. */
    public static int modifierPrice(MenuItem item, String selections) {
        if (selections == null || selections.isBlank()) {
            if (!item.requiredModifierGroups().isEmpty()) throw new IllegalArgumentException("required modifier missing");
            return 0;
        }
        int price = 0;
        Set<String> groups = new LinkedHashSet<>();
        for (String selection : selections.split("\\|", -1)) {
            if (selection.isBlank() || !groups.add(groupOf(selection))) {
                throw new IllegalArgumentException("invalid modifier selection");
            }
            Integer amount = item.modifiers().get(selection);
            if (amount == null) throw new IllegalArgumentException("unknown modifier");
            price = Math.addExact(price, amount);
        }
        if (!groups.containsAll(item.requiredModifierGroups())) throw new IllegalArgumentException("required modifier missing");
        return price;
    }

    public static String canonicalModifiers(String selections) {
        if (selections == null || selections.isBlank()) return "";
        return java.util.Arrays.stream(selections.split("\\|", -1)).sorted().reduce((a, b) -> a + "|" + b)
                .orElseThrow(() -> new IllegalArgumentException("invalid modifier selection"));
    }

    private static String groupOf(String selection) {
        int separator = selection.indexOf(':');
        return separator > 0 ? selection.substring(0, separator) : selection;
    }
}
