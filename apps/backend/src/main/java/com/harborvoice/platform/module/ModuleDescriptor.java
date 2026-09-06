package com.harborvoice.platform.module;

import java.util.Objects;

public record ModuleDescriptor(String moduleId, String version, String displayName) {
    public ModuleDescriptor {
        if (moduleId == null || !moduleId.matches("[a-z][a-z0-9-]{1,62}")) {
            throw new IllegalArgumentException("moduleId must be a lowercase stable identifier");
        }
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(displayName, "displayName");
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
    }
}
