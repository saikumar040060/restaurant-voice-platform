package com.harborvoice.platform.knowledge;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GroundedKnowledgeService {
    private final KnowledgePort knowledge;

    public GroundedKnowledgeService(KnowledgePort knowledge) { this.knowledge = knowledge; }

    public GroundedContext context(UUID businessId, UUID locationId, String query) {
        return new GroundedContext(query, knowledge.approved(businessId, locationId, query).stream()
                .map(item -> new GroundedContext.Source(item.id(), item.key(), item.content(),
                        item.provenance(), item.version())).toList());
    }
}
