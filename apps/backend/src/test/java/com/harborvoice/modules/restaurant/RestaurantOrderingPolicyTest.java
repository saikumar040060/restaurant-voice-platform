package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class RestaurantOrderingPolicyTest {
    @Test void appliesTenantQuantityCapTaxRoundingAndTransferThreshold() {
        var policy = HarborPizzaFixture.orderingPolicy();
        var item = new MenuItem("X", "Fictional item", 999, java.util.Map.of());
        var quote = OrderPricing.quote(List.of(new OrderPricing.Line(item, null, 10)), policy);

        assertThat(OrderTotals.from(quote, policy)).isEqualTo(new OrderTotals(9990, 599, 10589, "USD"));
        assertThat(policy.requiresLargeOrderTransfer(quote.subtotalMinor())).isFalse();
        assertThat(policy.requiresLargeOrderTransfer(10_000)).isTrue();
        assertThatThrownBy(() -> OrderPricing.quote(List.of(new OrderPricing.Line(item, null, 21)), policy))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
