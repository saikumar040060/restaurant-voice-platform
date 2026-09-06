package com.harborvoice.modules.restaurant;

public final class OrderLifecycle {
    private OrderLifecycle() { }

    public static OrderState transition(OrderState current, OrderState next) {
        if (current == null || next == null) throw new IllegalArgumentException("order state required");
        if (current == OrderState.ACCEPTED || current == OrderState.UNKNOWN || current == OrderState.CANCELLED) {
            throw new IllegalArgumentException("terminal order cannot transition");
        }
        boolean allowed = switch (current) {
            case DRAFT -> next == OrderState.CONFIRMED || next == OrderState.CANCELLED;
            case CONFIRMED -> next == OrderState.SUBMITTING || next == OrderState.CANCELLED;
            case SUBMITTING -> next == OrderState.ACCEPTED || next == OrderState.UNKNOWN;
            default -> false;
        };
        if (!allowed) throw new IllegalArgumentException("invalid order transition");
        return next;
    }
}
