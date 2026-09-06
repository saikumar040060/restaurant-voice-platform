package com.harborvoice.platform.audit;

import java.util.UUID;

public interface ConsentPort {
    void record(ConsentRecord consent);
    boolean granted(UUID businessId, UUID conversationId, ConsentPurpose purpose);
}
