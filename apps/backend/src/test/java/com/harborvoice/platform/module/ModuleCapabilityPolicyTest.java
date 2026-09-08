package com.harborvoice.platform.module;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.harborvoice.modules.restaurant.RestaurantBusinessModule;
import org.junit.jupiter.api.Test;

class ModuleCapabilityPolicyTest {
    @Test void resolvesOnlyCapabilitiesDeclaredByTheActiveModule() {
        var restaurant = new RestaurantBusinessModule();
        var capability = ModuleCapabilityPolicy.require(restaurant, "restaurant.quote");

        assertThat(capability.confirmationRequired()).isTrue();
        assertThat(capability.mutating()).isFalse();
        assertThatThrownBy(() -> ModuleCapabilityPolicy.require(new ReferenceBusinessModule(), "restaurant.quote"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tool is not enabled for module");
    }

    @Test void rejectsMissingAndMalformedToolLookup() {
        var reference = new ReferenceBusinessModule();
        assertThatThrownBy(() -> ModuleCapabilityPolicy.require(reference, ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ModuleCapabilityPolicy.require(null, "reference.faq"))
                .isInstanceOf(NullPointerException.class);
    }
}
