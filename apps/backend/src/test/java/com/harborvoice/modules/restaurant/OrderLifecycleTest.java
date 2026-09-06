package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class OrderLifecycleTest {
    @Test void permitsUnknownSubmissionOutcomeForReconciliation() {
        assertThat(OrderLifecycle.transition(OrderState.SUBMITTING, OrderState.UNKNOWN)).isEqualTo(OrderState.UNKNOWN);
        assertThatThrownBy(() -> OrderLifecycle.transition(OrderState.UNKNOWN, OrderState.SUBMITTING))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
