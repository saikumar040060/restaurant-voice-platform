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
}
