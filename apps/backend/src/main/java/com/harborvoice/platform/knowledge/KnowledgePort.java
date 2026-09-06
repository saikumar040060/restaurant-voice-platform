package com.harborvoice.platform.knowledge;

import java.util.List;
import java.util.UUID;

public interface KnowledgePort {
    List<KnowledgeRecord> approved(UUID businessId, UUID locationId, String query);
}
