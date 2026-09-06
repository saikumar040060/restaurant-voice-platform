package com.harborvoice.modules.restaurant;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcRestaurantOrderRepository {
    private final JdbcTemplate jdbc;

    public JdbcRestaurantOrderRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void storeConfirmed(java.util.UUID businessId, java.util.UUID locationId, OrderSubmission submission) {
        if (businessId == null || locationId == null || submission == null) throw new IllegalArgumentException("order scope required");
        jdbc.update("""
                INSERT INTO restaurant_orders(id, business_id, location_id, quote_hash, currency, total_minor, state)
                VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED')
                ON CONFLICT (id) DO NOTHING
                """, submission.orderId(), businessId, locationId, submission.quoteHash(),
                submission.quote().currency(), submission.quote().subtotalMinor());
    }

    public boolean transition(java.util.UUID businessId, java.util.UUID orderId, OrderState from, OrderState to) {
        if (from == null || to == null || from == OrderState.ACCEPTED || from == OrderState.UNKNOWN || from == OrderState.CANCELLED) {
            throw new IllegalArgumentException("terminal order cannot transition");
        }
        return jdbc.update("UPDATE restaurant_orders SET state = ? WHERE id = ? AND business_id = ? AND state = ?",
                to.name(), orderId, businessId, from.name()) == 1;
    }
}
