package com.harborvoice.modules.restaurant;

/** Deterministic order totals using half-up rounding at the integer minor-unit boundary. */
public record OrderTotals(int subtotalMinor, int taxMinor, int totalMinor, String currency) {
    public static OrderTotals from(OrderPricing.Quote quote, RestaurantOrderingPolicy policy) {
        if (quote == null || policy == null || !quote.currency().equals(policy.currency())) {
            throw new IllegalArgumentException("quote and policy currency required");
        }
        long scaledTax = Math.multiplyExact((long) quote.subtotalMinor(), policy.taxBasisPoints());
        int tax = Math.toIntExact((scaledTax + 5_000L) / 10_000L);
        return new OrderTotals(quote.subtotalMinor(), tax, Math.addExact(quote.subtotalMinor(), tax), quote.currency());
    }
}
