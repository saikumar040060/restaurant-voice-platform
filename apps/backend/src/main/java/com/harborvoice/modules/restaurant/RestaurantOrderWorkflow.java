package com.harborvoice.modules.restaurant;

public final class RestaurantOrderWorkflow {
    public record State(OrderState orderState, OrderDraft draft) { }

    public State initial(OrderDraft draft) {
        if (draft == null) throw new IllegalArgumentException("draft required");
        return new State(OrderState.DRAFT, draft);
    }

    public State confirm(State state) {
        if (state == null || state.draft() == null) throw new IllegalArgumentException("state required");
        return new State(OrderLifecycle.transition(state.orderState(), OrderState.CONFIRMED),
                state.draft().confirm(state.draft().quote()));
    }

    public State beginSubmission(State state) {
        if (state == null || !state.draft().confirmed()) throw new IllegalArgumentException("confirmed draft required");
        return new State(OrderLifecycle.transition(state.orderState(), OrderState.SUBMITTING), state.draft());
    }
}
