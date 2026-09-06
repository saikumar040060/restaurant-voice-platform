package com.harborvoice.platform.module;

import java.util.Objects;

public record ModuleDescriptor(String moduleId, String version, String displayName) {
    public ModuleDescriptor {
        if (moduleId == null || !moduleId.matches("[a-z][a-z0-9-]{1,62}")) {
            throw new IllegalArgumentException("moduleId must be a lowercase stable identifier");
        }
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(displayName, "displayName");
        if (!version.matches("[0-9]+\\.[0-9]+\\.[0-9]+") || displayName.isBlank() || displayName.length() > 160) {
            throw new IllegalArgumentException("invalid module descriptor");
        }
    }
}
