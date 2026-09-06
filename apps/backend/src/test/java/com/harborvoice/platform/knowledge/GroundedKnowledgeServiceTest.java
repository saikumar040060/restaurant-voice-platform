package com.harborvoice.platform.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroundedKnowledgeServiceTest {
    @Test void preservesSourceProvenanceForGrounding() {
        UUID business = UUID.randomUUID();
        var source = new KnowledgeRecord(UUID.randomUUID(), business, null, "hours", "Open 9-5", "owner", 3,
                KnowledgeRecord.ApprovalState.APPROVED, null);
        var service = new GroundedKnowledgeService((b, l, q) -> List.of(source));
        var context = service.context(business, null, "hours");
        assertThat(context.sources()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(source.id());
            assertThat(item.provenance()).isEqualTo("owner");
            assertThat(item.version()).isEqualTo(3);
        });
    }
}
