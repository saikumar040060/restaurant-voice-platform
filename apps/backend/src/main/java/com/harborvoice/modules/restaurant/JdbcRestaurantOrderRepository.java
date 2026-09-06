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
}
