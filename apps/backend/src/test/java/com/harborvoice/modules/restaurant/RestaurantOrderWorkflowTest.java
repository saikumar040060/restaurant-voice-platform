package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RestaurantOrderWorkflowTest {
    @Test void confirmationPrecedesSubmission() {
        var item = new MenuItem("tea", "Tea", 300, java.util.Map.of());
        var draft = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD");
        var workflow = new RestaurantOrderWorkflow();
        var state = workflow.initial(draft);
        assertThatThrownBy(() -> workflow.confirm(state))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("delivered read-back evidence required");
        var confirmed = workflow.confirm(state, RestaurantReadback.issue(draft, 1).markDelivered(1), 1);
        assertThat(confirmed.orderState()).isEqualTo(OrderState.CONFIRMED);
        assertThat(workflow.beginSubmission(confirmed).orderState()).isEqualTo(OrderState.SUBMITTING);
    }

    @Test void deliveredReadbackCanDriveSafeConfirmation() {
        var item = new MenuItem("tea", "Tea", 300, java.util.Map.of());
        var workflow = new RestaurantOrderWorkflow();
        var state = workflow.initial(OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD"));
        var delivered = RestaurantReadback.issue(state.draft(), 1).markDelivered(1);
        assertThat(workflow.confirm(state, delivered, 1).orderState()).isEqualTo(OrderState.CONFIRMED);
    }

    @Test void confirmedDraftCanReachOnlyTheMockPosBoundary() {
        var item = new MenuItem("tea", "Tea", 300, java.util.Map.of());
        var draft = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD");
        var workflow = new RestaurantOrderWorkflow();
        var confirmed = workflow.confirm(workflow.initial(draft), RestaurantReadback.issue(draft, 1).markDelivered(1), 1);
        assertThat(workflow.submit(confirmed, new FixtureOrderPort()).orderState()).isEqualTo(OrderState.ACCEPTED);
        assertThat(workflow.submit(confirmed, submission -> null).orderState()).isEqualTo(OrderState.UNKNOWN);
    }
}
