package com.harborvoice.platform.module;

public record ToolCapability(String toolId, boolean confirmationRequired, boolean mutating) {
    public ToolCapability {
        if (toolId == null || !toolId.matches("[a-z][a-z0-9_.-]{1,119}")) {
            throw new IllegalArgumentException("invalid tool identifier");
        }
    }
}
