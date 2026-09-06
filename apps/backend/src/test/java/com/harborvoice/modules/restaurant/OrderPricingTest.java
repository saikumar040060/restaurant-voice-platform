package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderPricingTest {
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
}
