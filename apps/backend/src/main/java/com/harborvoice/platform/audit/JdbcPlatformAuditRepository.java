package com.harborvoice.platform.audit;

import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPlatformAuditRepository implements PlatformAuditPort {
    private final JdbcTemplate jdbc;
    public JdbcPlatformAuditRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void append(PlatformAuditEvent event) {
        jdbc.update("""
                INSERT INTO audit_events(id, tenant_id, actor_id, action, target_id, outcome, correlation_id, occurred_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, event.id(), event.businessId(), event.actorId(), event.action(), event.targetId(),
                event.outcome(), event.correlationId(), Timestamp.from(event.occurredAt()));
    }
}
