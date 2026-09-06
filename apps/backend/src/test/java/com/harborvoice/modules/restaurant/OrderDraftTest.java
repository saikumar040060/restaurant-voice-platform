package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
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
}
