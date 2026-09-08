package com.harborvoice.modules.restaurant;

import com.harborvoice.identity.Actor;
import com.harborvoice.platform.audit.PlatformAuditEvent;
import com.harborvoice.platform.audit.PlatformAuditPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                SELECT business_id, item_index, decision, correction, rationale, version, publication_state, draft_revision, actor_id, decided_at
                FROM menu_review_decisions WHERE business_id = ? AND draft_revision = ? ORDER BY item_index
                """, (rs, ignored) -> fromRow(rs), actor.tenantId(), MenuReviewDraft.REVISION);
    }

    public MenuReviewSummary summary(Actor actor, int totalItems) {
        requireOwner(actor);
        if (totalItems != MenuReviewDraft.ITEM_COUNT) {
            throw new IllegalArgumentException("current draft item count required");
        }
        return jdbc.queryForObject("""
                SELECT
                    COUNT(*) FILTER (WHERE decision = 'APPROVED'),
                    COUNT(*) FILTER (WHERE decision = 'CORRECTED'),
                    COUNT(*) FILTER (WHERE decision = 'REJECTED')
                FROM menu_review_decisions
                WHERE business_id = ? AND draft_revision = ? AND publication_state = 'UNPUBLISHED'
                """, (rs, ignored) -> {
            int approved = rs.getInt(1);
            int corrected = rs.getInt(2);
            int rejected = rs.getInt(3);
            return new MenuReviewSummary(approved, corrected, rejected,
                    Math.max(0, totalItems - approved - corrected - rejected), "UNPUBLISHED");
        }, actor.tenantId(), MenuReviewDraft.REVISION);
    }

    public MenuReviewCompletion completion(Actor actor) {
        requireOwner(actor);
        return jdbc.query("""
                SELECT business_id, draft_revision, decision_set_hash, publication_state, completed_by, completed_at
                FROM menu_review_completions WHERE business_id = ? AND draft_revision = ?
                """, (rs, ignored) -> new MenuReviewCompletion(rs.getObject("business_id", UUID.class),
                rs.getString("draft_revision"), rs.getString("decision_set_hash"), rs.getString("publication_state"),
                rs.getObject("completed_by", UUID.class), rs.getTimestamp("completed_at").toInstant()),
                actor.tenantId(), MenuReviewDraft.REVISION).stream().findFirst().orElse(null);
    }

    public UUID completionAuditEventId(Actor actor) {
        requireOwner(actor);
        UUID targetId = completionTargetId(actor.tenantId());
        return jdbc.query("""
                SELECT id FROM audit_events
                WHERE tenant_id = ? AND action = 'MENU_REVIEW_COMPLETED' AND target_id = ?
                ORDER BY occurred_at DESC LIMIT 1
                """, (rs, ignored) -> rs.getObject("id", UUID.class), actor.tenantId(), targetId)
                .stream().findFirst().orElse(null);
    }

    @Transactional
    public MenuReviewCompletion complete(Actor actor) {
        requireOwner(actor);
        List<MenuReviewDecision> current = decisions(actor);
        if (current.size() != MenuReviewDraft.ITEM_COUNT) {
            throw new IllegalStateException("all current draft entries require an owner decision");
        }
        String decisionSetHash = decisionSetHash(current);
        int inserted = jdbc.update("""
                INSERT INTO menu_review_completions
                    (business_id, draft_revision, decision_set_hash, completed_by)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (business_id, draft_revision) DO NOTHING
                """, actor.tenantId(), MenuReviewDraft.REVISION, decisionSetHash, actor.employeeId());
        if (inserted == 1) {
            audit.append(new PlatformAuditEvent(UUID.randomUUID(), actor.tenantId(), actor.employeeId(),
                    "MENU_REVIEW_COMPLETED", completionTargetId(actor.tenantId()),
                    "UNPUBLISHED", UUID.randomUUID(), Instant.now()));
        }
        MenuReviewCompletion completion = completion(actor);
        if (completion == null || !completion.decisionSetHash().equals(decisionSetHash)) {
            throw new IllegalStateException("menu review completion conflict");
        }
        return completion;
    }

    @Transactional
    public MenuReviewDecision decide(Actor actor, int itemIndex, MenuReviewDecision.Decision decision,
                                     String correction, String rationale, int expectedVersion) {
        return write(actor, new DecisionWrite(itemIndex, decision, correction, rationale, expectedVersion));
    }

    @Transactional
    public List<MenuReviewDecision> decideAll(Actor actor, List<DecisionWrite> writes) {
        requireOwner(actor);
        if (writes == null || writes.isEmpty() || writes.size() > 256) {
            throw new IllegalArgumentException("between one and 256 review decisions required");
        }
        Set<Integer> indexes = new HashSet<>();
        for (DecisionWrite write : writes) {
            validate(write);
            if (write.expectedVersion() != 0) {
                throw new IllegalArgumentException("bulk review can decide only previously undecided entries");
            }
            if (!indexes.add(write.itemIndex())) {
                throw new IllegalArgumentException("duplicate item decision in bulk request");
            }
        }
        return writes.stream().map(write -> write(actor, write)).toList();
    }

    private MenuReviewDecision write(Actor actor, DecisionWrite write) {
        requireOwner(actor);
        validate(write);
        if (completion(actor) != null) {
            throw new IllegalStateException("completed menu review decisions are immutable");
        }
        int itemIndex = write.itemIndex();
        MenuReviewDecision.Decision decision = write.decision();
        String fixed = write.correction() == null ? null : write.correction().trim();
        String reason = write.rationale() == null ? null : write.rationale().trim();
        int expectedVersion = write.expectedVersion();
        int changed = jdbc.update("""
                INSERT INTO menu_review_decisions(business_id,item_index,decision,correction,rationale,version,actor_id,draft_revision)
                VALUES(?,?,?,?,?,?,?,?)
                ON CONFLICT (business_id,item_index) DO UPDATE SET
                    decision = EXCLUDED.decision,
                    correction = EXCLUDED.correction,
                    rationale = EXCLUDED.rationale,
                    version = EXCLUDED.version,
                    actor_id = EXCLUDED.actor_id,
                    decided_at = CURRENT_TIMESTAMP
                WHERE menu_review_decisions.version = ? AND menu_review_decisions.draft_revision = ?
                """, actor.tenantId(), itemIndex, decision.name(), fixed, reason, expectedVersion + 1,
                actor.employeeId(), MenuReviewDraft.REVISION, expectedVersion, MenuReviewDraft.REVISION);
        if (changed != 1) {
            throw new IllegalStateException("review decision version conflict");
        }
        UUID targetId = UUID.nameUUIDFromBytes((actor.tenantId() + ":" + itemIndex)
                .getBytes(StandardCharsets.UTF_8));
        audit.append(new PlatformAuditEvent(UUID.randomUUID(), actor.tenantId(), actor.employeeId(),
                "MENU_REVIEW_" + decision.name(), targetId, "UNPUBLISHED", UUID.randomUUID(), Instant.now()));
        return jdbc.queryForObject("""
                SELECT business_id, item_index, decision, correction, rationale, version, publication_state, draft_revision, actor_id, decided_at
                FROM menu_review_decisions WHERE business_id = ? AND item_index = ? AND draft_revision = ?
                """, (rs, ignored) -> fromRow(rs), actor.tenantId(), itemIndex, MenuReviewDraft.REVISION);
    }

    private static void validate(DecisionWrite write) {
        if (write == null || write.itemIndex() < 1 || write.itemIndex() > MenuReviewDraft.ITEM_COUNT
                || write.decision() == null || write.expectedVersion() < 0) {
            throw new IllegalArgumentException("item, decision, and expected version required");
        }
        String fixed = write.correction() == null ? null : write.correction().trim();
        String reason = write.rationale() == null ? null : write.rationale().trim();
        if ((fixed != null && fixed.length() > 2000) || (reason != null && reason.length() > 2000)) {
            throw new IllegalArgumentException("correction too long");
        }
        if ((write.decision() == MenuReviewDecision.Decision.CORRECTED) != (fixed != null && !fixed.isBlank())) {
            throw new IllegalArgumentException("correction state mismatch");
        }
        if ((write.decision() == MenuReviewDecision.Decision.REJECTED) != (reason != null && !reason.isBlank())) {
            throw new IllegalArgumentException("rejection rationale required");
        }
    }

    public record DecisionWrite(int itemIndex, MenuReviewDecision.Decision decision, String correction,
                                String rationale, int expectedVersion) { }

    private static String decisionSetHash(List<MenuReviewDecision> decisions) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (MenuReviewDecision decision : decisions) {
                String canonical = decision.itemIndex() + "|" + decision.decision() + "|"
                        + String.valueOf(decision.correction()) + "|" + String.valueOf(decision.rationale())
                        + "|" + decision.version() + "\n";
                digest.update(canonical.getBytes(StandardCharsets.UTF_8));
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private static UUID completionTargetId(UUID businessId) {
        return UUID.nameUUIDFromBytes((businessId + ":" + MenuReviewDraft.REVISION)
                .getBytes(StandardCharsets.UTF_8));
    }

    private static void requireOwner(Actor actor) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new IllegalArgumentException("owner review access required");
        }
    }

    private static MenuReviewDecision fromRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new MenuReviewDecision(rs.getObject("business_id", UUID.class), rs.getInt("item_index"),
                MenuReviewDecision.Decision.valueOf(rs.getString("decision")), rs.getString("correction"), rs.getString("rationale"),
                rs.getInt("version"), rs.getString("publication_state"), rs.getString("draft_revision"),
                rs.getObject("actor_id", UUID.class), rs.getTimestamp("decided_at").toInstant());
    }
}
