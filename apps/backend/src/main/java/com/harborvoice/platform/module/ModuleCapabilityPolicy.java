package com.harborvoice.platform.module;

import java.util.Objects;

/** Resolves only a reviewed tool declared by the active compiled business module. */
public final class ModuleCapabilityPolicy {
    private ModuleCapabilityPolicy() { }

    public static ToolCapability require(BusinessModule module, String toolId) {
        Objects.requireNonNull(module, "module required");
        if (toolId == null || toolId.isBlank()) throw new IllegalArgumentException("tool required");
        return module.tools().stream()
                .filter(capability -> capability.toolId().equals(toolId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("tool is not enabled for module"));
    }
}
