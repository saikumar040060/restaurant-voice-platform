package com.harborvoice.modules.restaurant;

public final class RestaurantOrderWorkflow {
    public record State(OrderState orderState, OrderDraft draft) { }

    public State initial(OrderDraft draft) {
        if (draft == null) throw new IllegalArgumentException("draft required");
        return new State(OrderState.DRAFT, draft);
    }

    /**
     * Kept only to fail safely for callers migrating to the read-back-aware method.
     * A restaurant order cannot become confirmed without delivered read-back evidence.
     */
    @Deprecated(forRemoval = true)
    public State confirm(State state) {
        if (state == null || state.draft() == null) throw new IllegalArgumentException("state required");
        throw new IllegalStateException("delivered read-back evidence required");
    }

    /** Confirmation through this path requires a completed deterministic read-back. */
    public State confirm(State state, RestaurantReadback readback, long confirmationEpoch) {
        if (state == null || readback == null) throw new IllegalArgumentException("state and read-back required");
        OrderDraft confirmed = readback.confirm(state.draft(), confirmationEpoch);
        return new State(OrderLifecycle.transition(state.orderState(), OrderState.CONFIRMED), confirmed);
    }

    public State beginSubmission(State state) {
        if (state == null || !state.draft().confirmed()) throw new IllegalArgumentException("confirmed draft required");
        return new State(OrderLifecycle.transition(state.orderState(), OrderState.SUBMITTING), state.draft());
    }

    /**
     * The only module-owned path to the POS boundary. It accepts an already-confirmed,
     * read-back-backed draft and preserves an uncertain adapter response for reconciliation.
     */
    public State submit(State state, OrderPort orders) {
        if (orders == null) throw new IllegalArgumentException("order port required");
        State submitting = beginSubmission(state);
        OrderPort.Result result = orders.submit(OrderSubmission.from(submitting.draft()));
        if (result == null || result.state() == null) {
            return new State(OrderState.UNKNOWN, submitting.draft());
        }
        if (result.state() != OrderState.ACCEPTED && result.state() != OrderState.UNKNOWN) {
            throw new IllegalStateException("invalid order adapter outcome");
        }
        return new State(result.state(), submitting.draft());
    }
}
