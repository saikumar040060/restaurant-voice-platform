package com.harborvoice.modules.restaurant;

import com.harborvoice.identity.Actor;
import com.harborvoice.platform.audit.PlatformAuditEvent;
import com.harborvoice.platform.audit.PlatformAuditPort;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcMenuReviewRepository {
    private final JdbcTemplate jdbc;
    private final PlatformAuditPort audit;

    public JdbcMenuReviewRepository(JdbcTemplate jdbc, PlatformAuditPort audit) {
        this.jdbc = jdbc;
        this.audit = audit;
    }

    public List<MenuReviewDecision> decisions(Actor actor) {
        requireOwner(actor);
        return jdbc.query("""
                SELECT business_id, item_index, decision, correction, version, publication_state, actor_id, decided_at
                FROM menu_review_decisions WHERE business_id = ? ORDER BY item_index
                """, (rs, ignored) -> fromRow(rs), actor.tenantId());
    }

    @Transactional
    public MenuReviewDecision decide(Actor actor, int itemIndex, MenuReviewDecision.Decision decision,
                                     String correction, int expectedVersion) {
        requireOwner(actor);
        if (itemIndex < 1 || decision == null || expectedVersion < 0) {
            throw new IllegalArgumentException("item, decision, and expected version required");
        }
        String fixed = correction == null ? null : correction.trim();
        if (fixed != null && fixed.length() > 2000) {
            throw new IllegalArgumentException("correction too long");
        }
        if ((decision == MenuReviewDecision.Decision.CORRECTED) != (fixed != null && !fixed.isBlank())) {
            throw new IllegalArgumentException("correction state mismatch");
        }
        int changed = jdbc.update("""
                INSERT INTO menu_review_decisions(business_id,item_index,decision,correction,version,actor_id)
                VALUES(?,?,?,?,?,?)
                ON CONFLICT (business_id,item_index) DO UPDATE SET
                    decision = EXCLUDED.decision,
                    correction = EXCLUDED.correction,
                    version = EXCLUDED.version,
                    actor_id = EXCLUDED.actor_id,
                    decided_at = CURRENT_TIMESTAMP
                WHERE menu_review_decisions.version = ?
                """, actor.tenantId(), itemIndex, decision.name(), fixed, expectedVersion + 1,
                actor.employeeId(), expectedVersion);
        if (changed != 1) {
            throw new IllegalStateException("review decision version conflict");
        }
        UUID targetId = UUID.nameUUIDFromBytes((actor.tenantId() + ":" + itemIndex)
                .getBytes(StandardCharsets.UTF_8));
        audit.append(new PlatformAuditEvent(UUID.randomUUID(), actor.tenantId(), actor.employeeId(),
                "MENU_REVIEW_" + decision.name(), targetId, "UNPUBLISHED", UUID.randomUUID(), Instant.now()));
        return jdbc.queryForObject("""
                SELECT business_id, item_index, decision, correction, version, publication_state, actor_id, decided_at
                FROM menu_review_decisions WHERE business_id = ? AND item_index = ?
                """, (rs, ignored) -> fromRow(rs), actor.tenantId(), itemIndex);
    }

    private static void requireOwner(Actor actor) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new IllegalArgumentException("owner review access required");
        }
    }

    private static MenuReviewDecision fromRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new MenuReviewDecision(rs.getObject("business_id", UUID.class), rs.getInt("item_index"),
                MenuReviewDecision.Decision.valueOf(rs.getString("decision")), rs.getString("correction"),
                rs.getInt("version"), rs.getString("publication_state"),
                rs.getObject("actor_id", UUID.class), rs.getTimestamp("decided_at").toInstant());
    }
}
