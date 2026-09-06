package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RestaurantOrderWorkflowTest {
    @Test void confirmationPrecedesSubmission() {
        var item = new MenuItem("tea", "Tea", 300, java.util.Map.of());
        var draft = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD");
        var workflow = new RestaurantOrderWorkflow();
        var confirmed = workflow.confirm(workflow.initial(draft));
        assertThat(confirmed.orderState()).isEqualTo(OrderState.CONFIRMED);
        assertThat(workflow.beginSubmission(confirmed).orderState()).isEqualTo(OrderState.SUBMITTING);
    }
}
