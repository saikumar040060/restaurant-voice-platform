package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.harborvoice.platform.escalation.EscalationCase;
import org.junit.jupiter.api.Test;

class RestaurantEscalationPolicyTest {
    @Test void everyRequiredRestaurantEscalationHasAnExplicitSafeRoute() {
        for (var category : RestaurantEscalationPolicy.Category.values()) {
            var decision = RestaurantEscalationPolicy.classify(category);
            assertThat(decision.category()).isEqualTo(category);
            assertThat(decision.route()).isIn(RestaurantEscalationPolicy.Route.TRANSFER,
                    RestaurantEscalationPolicy.Route.CALLBACK);
        }
        assertThat(RestaurantEscalationPolicy.classify(RestaurantEscalationPolicy.Category.EMPLOYEE_UNAVAILABLE).route())
                .isEqualTo(RestaurantEscalationPolicy.Route.CALLBACK);
        assertThat(RestaurantEscalationPolicy.classify(RestaurantEscalationPolicy.Category.ALLERGY_OR_CROSS_CONTAMINATION).reason())
                .isEqualTo(EscalationCase.Reason.POLICY_BLOCK);
        assertThat(RestaurantEscalationPolicy.classify(RestaurantEscalationPolicy.Category.UNCERTAIN_ORDER).reason())
                .isEqualTo(EscalationCase.Reason.SYSTEM_FAILURE);
    }

    @Test void missingCategoryIsRejected() {
        assertThatThrownBy(() -> RestaurantEscalationPolicy.classify(null))
                .isInstanceOf(NullPointerException.class);
    }
}
