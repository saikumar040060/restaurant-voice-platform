package com.harborvoice.platform.tenancy;

import java.util.UUID;

public record BusinessScope(UUID businessId, UUID locationId) {
    public BusinessScope {
        if (businessId == null) throw new IllegalArgumentException("businessId required");
    }

    public boolean includes(UUID candidateBusiness, UUID candidateLocation) {
        return businessId.equals(candidateBusiness) && (locationId == null || locationId.equals(candidateLocation));
    }
}
