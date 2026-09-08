package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderSubmissionHashTest {
    @Test void quote_hash_is_canonical_and_changes_when_a_confirmed_line_changes() {
        MenuItem cheese = new MenuItem("CHEESE-M", "Cheese", 1299, java.util.Map.of("extra", 200));
        OrderDraft original = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(cheese, "extra", 1)), "USD")
                .confirm(OrderPricing.quote(List.of(new OrderPricing.Line(cheese, "extra", 1)), "USD"));
        OrderDraft changed = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(cheese, "extra", 2)), "USD")
                .confirm(OrderPricing.quote(List.of(new OrderPricing.Line(cheese, "extra", 2)), "USD"));

        assertThat(OrderSubmission.from(original).quoteHash()).hasSize(64)
                .isEqualTo(OrderSubmission.from(original).quoteHash())
                .isNotEqualTo(OrderSubmission.from(changed).quoteHash());
    }
}
