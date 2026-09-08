package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderDraftTest {
    @Test void confirmationBindsToExactQuote() {
        var item = new MenuItem("soup", "Fictional Soup", 800, java.util.Map.of());
        var draft = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD");
        assertThat(draft.confirm(draft.quote()).confirmed()).isTrue();
        assertThatThrownBy(() -> draft.confirm(new OrderPricing.Quote(draft.quote().lines(), 801, "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void snapshotBoundConfirmationFailsWhenAvailabilityDrifts() {
        var item = new MenuItem("soup", "Fictional Soup", 800, Map.of());
        var current = new AvailabilitySnapshot("rev-1", Map.of("soup", true));
        var draft = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD", current);

        assertThat(draft.confirm(draft.quote(), current).confirmed()).isTrue();
        assertThatThrownBy(() -> draft.confirm(draft.quote(), new AvailabilitySnapshot("rev-2", Map.of("soup", false))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
