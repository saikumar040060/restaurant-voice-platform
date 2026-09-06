package com.harborvoice.platform.action;

import com.harborvoice.platform.module.ToolCapability;

/** Central capability check applied to read and mutating tools alike. */
public final class ToolAuthorization {
    private ToolAuthorization() { }

    public static void require(ToolCapability capability, ActionRequest request, boolean confirmationProvided) {
        if (capability == null || request == null || !capability.toolId().equals(request.toolId())
                || (capability.confirmationRequired() && !confirmationProvided)) {
            throw new IllegalArgumentException("tool authorization denied");
        }
    }
}
