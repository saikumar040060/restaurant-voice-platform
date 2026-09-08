package com.harborvoice.modules.restaurant;

import com.harborvoice.platform.escalation.EscalationCase;
import java.util.Objects;

/**
 * Restaurant-owned classification for requests that must leave automated ordering.
 * This class makes no contact and contains no customer data; callers create a
 * tenant-scoped case through the platform escalation port after classification.
 */
public final class RestaurantEscalationPolicy {
    public enum Category {
        REFUND_OR_VOID,
        PAYMENT_DISPUTE,
        COMPLAINT,
        FOOD_SAFETY_OR_INJURY,
        THREAT_OR_HARASSMENT,
        EMERGENCY,
        ALLERGY_OR_CROSS_CONTAMINATION,
        MANAGER_REQUEST,
        UNCERTAIN_ORDER,
        PROVIDER_FAILURE,
        EMPLOYEE_UNAVAILABLE
    }

    public enum Route { TRANSFER, CALLBACK }

    public record Decision(Category category, Route route, EscalationCase.Reason reason) {
        public Decision {
            if (category == null || route == null || reason == null) {
                throw new IllegalArgumentException("restaurant escalation decision required");
            }
        }
    }

    private RestaurantEscalationPolicy() { }

    public static Decision classify(Category category) {
        Objects.requireNonNull(category, "category required");
        return switch (category) {
            case EMPLOYEE_UNAVAILABLE -> new Decision(category, Route.CALLBACK, EscalationCase.Reason.SYSTEM_FAILURE);
            case COMPLAINT, MANAGER_REQUEST -> new Decision(category, Route.TRANSFER, EscalationCase.Reason.CUSTOMER_REQUEST);
            case PROVIDER_FAILURE, UNCERTAIN_ORDER -> new Decision(category, Route.TRANSFER, EscalationCase.Reason.SYSTEM_FAILURE);
            case REFUND_OR_VOID, PAYMENT_DISPUTE, FOOD_SAFETY_OR_INJURY, THREAT_OR_HARASSMENT,
                    EMERGENCY, ALLERGY_OR_CROSS_CONTAMINATION ->
                    new Decision(category, Route.TRANSFER, EscalationCase.Reason.POLICY_BLOCK);
        };
    }
}
