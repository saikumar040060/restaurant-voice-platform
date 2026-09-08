package com.harborvoice.modules.restaurant;

import java.util.concurrent.ConcurrentHashMap;

/** Local POS fixture. It never contacts or claims to create a real external order. */
public final class FixtureOrderPort implements OrderPort {
    private final ConcurrentHashMap<String, Result> accepted = new ConcurrentHashMap<>();

    @Override public Result submit(OrderSubmission submission) {
        if (submission == null) throw new IllegalArgumentException("submission required");
        String idempotencyKey = submission.orderId() + ":" + submission.quoteHash();
        return accepted.computeIfAbsent(idempotencyKey,
                ignored -> new Result(OrderState.ACCEPTED, "fixture-order-" + submission.orderId()));
    }
}
