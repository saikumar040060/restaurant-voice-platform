package com.harborvoice.platform.audit;

import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcConsentRepository implements ConsentPort {
    private final JdbcTemplate jdbc;

    public JdbcConsentRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void record(ConsentRecord consent) {
        jdbc.update("""
                INSERT INTO consent_records(id, business_id, conversation_id, purpose, granted, evidence_hash, recorded_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, consent.id(), consent.businessId(), consent.conversationId(), consent.purpose().name(),
                consent.granted(), consent.evidenceHash(), Timestamp.from(consent.recordedAt()));
    }

    @Override
    public boolean granted(UUID businessId, UUID conversationId, ConsentPurpose purpose) {
        return jdbc.query("""
                SELECT granted FROM consent_records
                WHERE business_id = ? AND conversation_id = ? AND purpose = ?
                ORDER BY recorded_at DESC LIMIT 1
                """, rs -> rs.next() && rs.getBoolean(1), businessId, conversationId, purpose.name());
    }
}
