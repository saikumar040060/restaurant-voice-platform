package com.harborvoice.modules.restaurant;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcRestaurantOrderRepository implements RestaurantOrderQuery {
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

    @Override
    public java.util.List<RestaurantOrderQuery.Summary> recent(java.util.UUID businessId, int limit) {
        if (businessId == null || limit < 1 || limit > 100) throw new IllegalArgumentException("order query scope required");
        return jdbc.query("""
                SELECT id, location_id, currency, total_minor, state, created_at
                FROM restaurant_orders WHERE business_id = ?
                ORDER BY created_at DESC, id DESC LIMIT ?
                """, (rs, row) -> new RestaurantOrderQuery.Summary(
                rs.getObject("id", java.util.UUID.class), rs.getObject("location_id", java.util.UUID.class),
                rs.getString("currency"), rs.getLong("total_minor"), OrderState.valueOf(rs.getString("state")),
                rs.getTimestamp("created_at").toInstant()), businessId, limit);
    }

    public boolean transition(java.util.UUID businessId, java.util.UUID orderId, OrderState from, OrderState to) {
        if (from == null || to == null || from == OrderState.ACCEPTED || from == OrderState.UNKNOWN || from == OrderState.CANCELLED) {
            throw new IllegalArgumentException("terminal order cannot transition");
        }
        return jdbc.update("UPDATE restaurant_orders SET state = ? WHERE id = ? AND business_id = ? AND state = ?",
                to.name(), orderId, businessId, from.name()) == 1;
    }

    public void storeLines(java.util.UUID orderId, java.util.List<OrderPricing.Line> lines) {
        if (orderId == null || lines == null || lines.isEmpty()) throw new IllegalArgumentException("order lines required");
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            if (line == null || line.item() == null || line.quantity() < 1) throw new IllegalArgumentException("invalid order line");
            int modifier = OrderPricing.modifierPrice(line.item(), line.modifier());
            if (modifier < 0) throw new IllegalArgumentException("unknown modifier");
            jdbc.update("""
                    INSERT INTO restaurant_order_lines(order_id, line_no, sku, modifier, quantity, unit_price_minor)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON CONFLICT (order_id, line_no) DO NOTHING
                    """, orderId, i, line.item().sku(), OrderPricing.canonicalModifiers(line.modifier()), line.quantity(), line.item().priceMinor() + modifier);
        }
    }
}
