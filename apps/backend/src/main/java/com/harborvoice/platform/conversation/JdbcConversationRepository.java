package com.harborvoice.platform.conversation;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcConversationRepository implements ConversationPort {
    private final JdbcTemplate jdbc;

    public JdbcConversationRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public ConversationState state(UUID conversationId, UUID businessId) {
        return jdbc.query("SELECT state FROM conversations WHERE id = ? AND business_id = ?",
                rs -> rs.next() ? ConversationState.valueOf(rs.getString(1)) : null, conversationId, businessId);
    }

    @Override
    @Transactional
    public void acceptTurn(ConversationTurn turn, UUID businessId) {
        int inserted = jdbc.update("""
                INSERT INTO conversation_turns(id, conversation_id, business_id, sequence_no, epoch, speaker, text, final_text, occurred_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, turn.turnId(), turn.conversationId(), businessId, turn.sequence(), turn.epoch(),
                turn.speaker(), turn.text(), turn.finalText(), Timestamp.from(turn.occurredAt()));
        if (inserted != 1) throw new IllegalStateException("conversation turn was not stored");
    }

    @Override
    @Transactional
    public void interrupt(UUID conversationId, UUID businessId, long nextEpoch) {
        int updated = jdbc.update("""
                UPDATE conversations SET current_epoch = ?
                WHERE id = ? AND business_id = ? AND current_epoch < ? AND state <> 'ENDED'
                """, nextEpoch, conversationId, businessId, nextEpoch);
        if (updated != 1) throw new IllegalArgumentException("invalid conversation interruption");
    }

    @Transactional
    public void start(UUID conversationId, UUID businessId) {
        jdbc.update("INSERT INTO conversations(id, business_id, state) VALUES (?, ?, 'ACTIVE')",
                conversationId, businessId);
    }
}
