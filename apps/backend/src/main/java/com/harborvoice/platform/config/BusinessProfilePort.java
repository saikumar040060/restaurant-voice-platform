package com.harborvoice.platform.config;

import java.util.UUID;

public interface BusinessProfilePort {
    BusinessProfile approved(UUID businessId);
}
