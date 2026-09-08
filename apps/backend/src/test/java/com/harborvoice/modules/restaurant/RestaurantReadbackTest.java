package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RestaurantReadbackTest {
    @Test void requiresA_delivered_uninterrupted_readback_for_confirmation() {
        var item = new MenuItem("tea", "Fictional Tea", 300, java.util.Map.of());
        var draft = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD");
        var readback = RestaurantReadback.issue(draft, 4);
        assertThatThrownBy(() -> readback.confirm(draft, 4)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> readback.markDelivered(5)).isInstanceOf(IllegalArgumentException.class);
        assertThat(readback.markDelivered(4).confirm(draft, 4).confirmed()).isTrue();
    }
}
