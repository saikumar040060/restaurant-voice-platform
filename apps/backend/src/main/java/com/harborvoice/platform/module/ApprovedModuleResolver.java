package com.harborvoice.platform.module;

import java.util.UUID;

/** Tenant-bound lookup of a reviewed, enabled business module. */
public interface ApprovedModuleResolver {
    BusinessModule requireApproved(UUID businessId, String moduleId);
}
