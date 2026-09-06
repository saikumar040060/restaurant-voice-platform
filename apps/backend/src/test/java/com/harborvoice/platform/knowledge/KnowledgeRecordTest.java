package com.harborvoice.platform.knowledge;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KnowledgeRecordTest {
    @Test void rejectsUnusableKnowledge() {
        assertThatThrownBy(() -> new KnowledgeRecord(UUID.randomUUID(), UUID.randomUUID(), null,
                "", "answer", "owner", 1, KnowledgeRecord.ApprovalState.DRAFT, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
