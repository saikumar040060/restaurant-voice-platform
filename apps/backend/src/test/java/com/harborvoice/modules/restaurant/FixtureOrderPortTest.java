package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FixtureOrderPortTest {
    @Test void acceptsOnlyConfirmedSubmission() {
        var item = new MenuItem("tea", "Fictional Tea", 300, java.util.Map.of());
        var draft = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD")
                .confirm(OrderPricing.quote(List.of(new OrderPricing.Line(item, null, 1)), "USD"));
        var result = new FixtureOrderPort().submit(OrderSubmission.from(draft));
        assertThat(result.state()).isEqualTo(OrderState.ACCEPTED);
    }
}
