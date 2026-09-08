package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderPricingTest {

    @Test
    void rejectsUnboundedLineCount() {
        var item = new MenuItem("x", "Pizza", 100, java.util.Map.of());
        var lines = java.util.stream.IntStream.range(0, 101)
                .mapToObj(i -> new OrderPricing.Line(item, null, 1)).toList();
        assertThatThrownBy(() -> OrderPricing.quote(lines, "USD")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void calculatesDeterministicMinorUnitQuote() {
        var item = new MenuItem("burger", "Fictional Burger", 1200, java.util.Map.of("cheese", 150));
        var quote = OrderPricing.quote(List.of(new OrderPricing.Line(item, "cheese", 2)), "USD");
        assertThat(quote.subtotalMinor()).isEqualTo(2700);
    }

    @Test void rejectsUnknownModifier() {
        var item = new MenuItem("burger", "Fictional Burger", 1200, java.util.Map.of());
        assertThatThrownBy(() -> OrderPricing.quote(List.of(new OrderPricing.Line(item, "cheese", 1)), "USD"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void requiresNamedModifierGroupsAndPricesMultipleGroupsDeterministically() {
        var byo = HarborPizzaFixture.menu().items().stream().filter(item -> item.sku().equals("BYO")).findFirst().orElseThrow();
        assertThatThrownBy(() -> OrderPricing.quote(List.of(new OrderPricing.Line(byo, "topping:onions", 1)), "USD"))
                .isInstanceOf(IllegalArgumentException.class);
        var quote = OrderPricing.quote(List.of(new OrderPricing.Line(byo, "size:medium|topping:onions", 2)), "USD");
        assertThat(quote.subtotalMinor()).isEqualTo(2748);
        assertThatThrownBy(() -> OrderPricing.quote(List.of(new OrderPricing.Line(byo, "size:small|size:large", 1)), "USD"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
