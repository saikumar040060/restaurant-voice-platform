package com.harborvoice.platform.tenancy;

import java.util.UUID;

/** Non-human principal for scoped workers; never interchangeable with staff actors. */
public record ServicePrincipal(UUID principalId, UUID businessId, String service) {
    public ServicePrincipal {
        if (principalId == null || businessId == null || service == null
                || !service.matches("[a-z][a-z0-9_.-]{1,63}")) {
            throw new IllegalArgumentException("invalid service principal");
        }
    }
}
