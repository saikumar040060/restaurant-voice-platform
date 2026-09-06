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
}
