package com.harborvoice.modules.restaurant;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Read-only restaurant-owned order projection. Customer details and provider data are excluded. */
public interface RestaurantOrderQuery {
    record Summary(UUID orderId, UUID locationId, String currency, long totalMinor, OrderState state, Instant createdAt) {
        public Summary {
            if (orderId == null || locationId == null || currency == null || currency.isBlank() || totalMinor < 0
                    || state == null || createdAt == null) {
                throw new IllegalArgumentException("valid restaurant order summary required");
            }
        }
    }

    List<Summary> recent(UUID businessId, int limit);
}
