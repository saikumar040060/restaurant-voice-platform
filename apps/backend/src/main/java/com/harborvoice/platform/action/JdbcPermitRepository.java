package com.harborvoice.platform.action;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPermitRepository {
    private final JdbcTemplate jdbc;

    public JdbcPermitRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void issue(PolicyGateway.ActionPermit permit) {
        jdbc.update("""
                INSERT INTO action_permits(request_id, business_id, tool_id, request_hash, expires_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (request_id) DO UPDATE SET request_hash = EXCLUDED.request_hash,
                    expires_at = EXCLUDED.expires_at
                """, permit.requestId(), permit.businessId(), permit.toolId(), permit.requestHash(),
                Timestamp.from(permit.expiresAt().value()));
    }

    public boolean valid(UUID requestId, UUID businessId, String toolId, String requestHash, Instant now) {
        return Boolean.TRUE.equals(jdbc.query("""
                SELECT EXISTS (SELECT 1 FROM action_permits
                 WHERE request_id = ? AND business_id = ? AND tool_id = ? AND request_hash = ? AND expires_at > ?)
                """, rs -> rs.next() && rs.getBoolean(1), requestId, businessId, toolId, requestHash, Timestamp.from(now)));
    }
}
