package com.harborvoice.platform.action;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcOutboxRepository {
    private static final int MAX_ATTEMPTS = 5;
    private final JdbcTemplate jdbc;
    public JdbcOutboxRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void enqueue(UUID outboxId, UUID requestId, UUID businessId) {
        if (outboxId == null || requestId == null || businessId == null) throw new IllegalArgumentException("outbox scope required");
        jdbc.update("INSERT INTO action_outbox(id, request_id, business_id) VALUES (?, ?, ?) ON CONFLICT (request_id) DO NOTHING",
                outboxId, requestId, businessId);
    }

    public boolean transition(UUID outboxId, UUID businessId, OutboxStatus from, OutboxStatus to) {
        if (from == null || to == null || from == OutboxStatus.RECONCILED) throw new IllegalArgumentException("invalid outbox transition");
        return jdbc.update("UPDATE action_outbox SET status = ? WHERE id = ? AND business_id = ? AND status = ?",
                to.name(), outboxId, businessId, from.name()) == 1;
    }

    public boolean incrementAttempt(UUID outboxId, UUID businessId) {
        return jdbc.update("UPDATE action_outbox SET attempts = attempts + 1 WHERE id = ? AND business_id = ? AND status IN ('PENDING', 'DISPATCHED') AND attempts < " + MAX_ATTEMPTS,
                outboxId, businessId) == 1;
    }

    public boolean deadLetter(UUID outboxId, UUID businessId) {
        return jdbc.update("UPDATE action_outbox SET status = 'DEAD_LETTER' WHERE id = ? AND business_id = ? AND status IN ('PENDING', 'DISPATCHED') AND attempts >= " + MAX_ATTEMPTS,
                outboxId, businessId) == 1;
    }
}
