package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.harborvoice.identity.Actor;
import com.harborvoice.platform.module.ApprovedModuleResolver;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class RestaurantMenuServiceTest {
    @Test void resolvesOnlyTheRestaurantModuleApprovedForTheAuthenticatedTenant() {
        var observedBusiness = new AtomicReference<UUID>();
        ApprovedModuleResolver bindings = (businessId, moduleId) -> {
            observedBusiness.set(businessId);
            if (!"restaurant".equals(moduleId)) throw new IllegalArgumentException("unexpected module");
            return new RestaurantBusinessModule();
        };
        var actor = new Actor(UUID.randomUUID(), UUID.randomUUID(), Actor.Role.EMPLOYEE);

        var menu = new RestaurantMenuService(bindings).approvedMenu(actor);

        assertThat(observedBusiness).hasValue(actor.tenantId());
        assertThat(menu.items()).hasSize(13);
    }

    @Test void rejectsUnauthenticatedMenuAccess() {
        ApprovedModuleResolver unused = (business, module) -> new RestaurantBusinessModule();
        assertThatThrownBy(() -> new RestaurantMenuService(unused).approvedMenu(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("authenticated tenant required");
    }
}
