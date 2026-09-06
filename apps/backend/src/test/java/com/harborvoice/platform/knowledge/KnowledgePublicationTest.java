package com.harborvoice.platform.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KnowledgePublicationTest {
    @Test void requiresNewerVersionForApproval() {
        var item = new KnowledgeRecord(UUID.randomUUID(), UUID.randomUUID(), null, "faq", "Answer", "owner", 2,
                KnowledgeRecord.ApprovalState.DRAFT, null);
        assertThat(KnowledgePublication.approve(item, 1).approvalState()).isEqualTo(KnowledgeRecord.ApprovalState.APPROVED);
        assertThatThrownBy(() -> KnowledgePublication.approve(item, 2)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void revokeRequiresApprovedAndRollbackRestoresOlderTenantVersion() {
        UUID business = UUID.randomUUID();
        var current = new KnowledgeRecord(UUID.randomUUID(), business, null, "hours", "Open 10-6", "owner", 3,
                KnowledgeRecord.ApprovalState.APPROVED, null);
        var older = new KnowledgeRecord(UUID.randomUUID(), business, null, "hours", "Open 9-5", "owner", 2,
                KnowledgeRecord.ApprovalState.DRAFT, null);
        assertThat(KnowledgePublication.revoke(current).approvalState()).isEqualTo(KnowledgeRecord.ApprovalState.REVOKED);
        assertThat(KnowledgePublication.rollback(older, current).approvalState()).isEqualTo(KnowledgeRecord.ApprovalState.APPROVED);
        assertThatThrownBy(() -> KnowledgePublication.revoke(older)).isInstanceOf(IllegalArgumentException.class);
        var other = new KnowledgeRecord(UUID.randomUUID(), UUID.randomUUID(), null, "hours", "x", "owner", 1,
                KnowledgeRecord.ApprovalState.DRAFT, null);
        assertThatThrownBy(() -> KnowledgePublication.rollback(other, current)).isInstanceOf(IllegalArgumentException.class);
    }
}
