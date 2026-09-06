package com.harborvoice.platform.module;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ModuleRegistryTest {
    @Test
    void resolvesReviewedReferenceModuleAndRejectsUnknownIds() {
        var reference = new ReferenceBusinessModule();
        var registry = new ModuleRegistry(java.util.List.of(reference));

        assertThat(registry.require("reference")).isSameAs(reference);
        assertThat(reference.supportedIntents()).containsExactlyInAnyOrder("faq", "callback_request", "human_transfer");
        assertThatThrownBy(() -> registry.require("restaurant")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void descriptorRejectsUnsafeIdentifiers() {
        assertThatThrownBy(() -> new ModuleDescriptor("../admin", "1", "Bad"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toolCapabilityRejectsPathTraversalIdentifiers() {
        assertThatThrownBy(() -> new ToolCapability("../admin", false, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsDuplicateModuleIds() {
        BusinessModule first = new ReferenceBusinessModule();
        BusinessModule second = new BusinessModule() {
            public ModuleDescriptor descriptor() { return new ModuleDescriptor("reference", "2.0.0", "Other"); }
            public Set<String> supportedIntents() { return Set.of(); }
        };
        assertThatThrownBy(() -> new ModuleRegistry(java.util.List.of(first, second)))
                .isInstanceOf(IllegalStateException.class);
    }
}
