package com.harborvoice.platform.ops;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class UsageBudgetTest {
    @Test void rejectsConsumptionPastPerCallLimits() {
        var budget = new UsageBudget(1000, 100);
        assertThat(budget.consume(700, 60)).isTrue();
        assertThat(budget.consume(400, 1)).isFalse();
        assertThat(budget.audioRemaining()).isEqualTo(300);
    }
}
