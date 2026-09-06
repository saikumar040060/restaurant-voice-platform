package com.harborvoice.platform.knowledge;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcKnowledgeRepository implements KnowledgePort {
    private final JdbcTemplate jdbc;

    public JdbcKnowledgeRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public List<KnowledgeRecord> approved(UUID businessId, UUID locationId, String query) {
        String pattern = "%" + (query == null ? "" : query.trim()) + "%";
        return jdbc.query("""
                SELECT id, business_id, location_id, canonical_key, content, provenance, version,
                       approval_state, fresh_until
                FROM knowledge_items
                WHERE business_id = ? AND approval_state = 'APPROVED'
                  AND (fresh_until IS NULL OR fresh_until > CURRENT_TIMESTAMP)
                  AND (location_id IS NULL OR location_id = ?)
                  AND (canonical_key ILIKE ? OR content ILIKE ?)
                ORDER BY version DESC, canonical_key
                LIMIT 20
                """, (rs, row) -> new KnowledgeRecord(rs.getObject("id", UUID.class),
                rs.getObject("business_id", UUID.class), rs.getObject("location_id", UUID.class),
                rs.getString("canonical_key"), rs.getString("content"), rs.getString("provenance"),
                rs.getInt("version"), KnowledgeRecord.ApprovalState.valueOf(rs.getString("approval_state")),
                rs.getObject("fresh_until", Timestamp.class) == null ? null : rs.getTimestamp("fresh_until").toInstant()),
                businessId, locationId, pattern, pattern);
    }
}
