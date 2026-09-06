package com.harborvoice.modules.restaurant;

/** Local POS fixture. It never contacts or claims to create a real external order. */
public final class FixtureOrderPort implements OrderPort {
    @Override public Result submit(OrderSubmission submission) {
        if (submission == null) throw new IllegalArgumentException("submission required");
        return new Result(OrderState.ACCEPTED, "fixture-order-" + submission.orderId());
    }
}
