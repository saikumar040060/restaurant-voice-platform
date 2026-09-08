package com.harborvoice.modules.restaurant;

import java.util.Objects;

/** Immutable, deterministic read-back evidence required before restaurant confirmation. */
public record RestaurantReadback(String quoteHash, long epoch, boolean delivered) {
    public RestaurantReadback {
        if (quoteHash == null || quoteHash.length() != 64 || epoch < 0) {
            throw new IllegalArgumentException("valid quote evidence required");
        }
    }

    public static RestaurantReadback issue(OrderDraft draft, long epoch) {
        Objects.requireNonNull(draft, "draft");
        if (draft.confirmed()) throw new IllegalArgumentException("already confirmed");
        return new RestaurantReadback(OrderSubmission.previewHash(draft.quote()), epoch, false);
    }

    public RestaurantReadback markDelivered(long playbackEpoch) {
        if (playbackEpoch != epoch) throw new IllegalArgumentException("read-back interrupted");
        return new RestaurantReadback(quoteHash, epoch, true);
    }

    public OrderDraft confirm(OrderDraft draft, long confirmationEpoch) {
        if (!delivered || confirmationEpoch != epoch || !quoteHash.equals(OrderSubmission.previewHash(draft.quote()))) {
            throw new IllegalArgumentException("fresh delivered read-back required");
        }
        return draft.confirm(draft.quote());
    }
}
