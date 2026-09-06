package com.harborvoice.modules.restaurant;

public interface OrderPort {
    Result submit(OrderSubmission submission);
    record Result(OrderState state, String externalReference) { }
}
