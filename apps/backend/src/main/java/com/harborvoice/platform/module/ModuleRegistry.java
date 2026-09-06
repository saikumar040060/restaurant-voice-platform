package com.harborvoice.platform.module;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** Provides only reviewed modules discovered through dependency injection. */
@Component
public class ModuleRegistry {
    private final Map<String, BusinessModule> modules;

    public ModuleRegistry(java.util.List<BusinessModule> modules) {
        modules.forEach(ModuleRegistry::validate);
        this.modules = modules.stream().collect(Collectors.toUnmodifiableMap(
                module -> module.descriptor().moduleId(), Function.identity(),
                (left, right) -> { throw new IllegalStateException("duplicate business module"); }));
    }

    private static void validate(BusinessModule module) {
        if (module == null || module.descriptor() == null || module.supportedIntents() == null
                || module.supportedIntents().stream().anyMatch(intent -> intent == null || !intent.matches("[a-z][a-z0-9_.-]{1,63}"))) {
            throw new IllegalArgumentException("invalid business module contract");
        }
        if (module.tools() == null) throw new IllegalArgumentException("module tools required");
    }

    public BusinessModule require(String moduleId) {
        Objects.requireNonNull(moduleId, "moduleId");
        BusinessModule module = modules.get(moduleId);
        if (module == null) {
            throw new IllegalArgumentException("unknown business module: " + moduleId);
        }
        return module;
    }
}
