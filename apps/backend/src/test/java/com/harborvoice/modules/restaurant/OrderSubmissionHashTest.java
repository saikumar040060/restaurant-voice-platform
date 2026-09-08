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

    @Test void modifier_selection_order_does_not_change_a_confirmed_quote_hash() {
        MenuItem byo = HarborPizzaFixture.menu().items().stream().filter(item -> item.sku().equals("BYO")).findFirst().orElseThrow();
        OrderDraft first = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(byo, "size:medium|topping:onions", 1)), "USD")
                .confirm(OrderPricing.quote(List.of(new OrderPricing.Line(byo, "size:medium|topping:onions", 1)), "USD"));
        OrderDraft reordered = OrderDraft.create(first.id(), List.of(new OrderPricing.Line(byo, "topping:onions|size:medium", 1)), "USD")
                .confirm(OrderPricing.quote(List.of(new OrderPricing.Line(byo, "topping:onions|size:medium", 1)), "USD"));

        assertThat(OrderSubmission.from(first).quoteHash()).isEqualTo(OrderSubmission.from(reordered).quoteHash());
    }
}
