package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.harborvoice.identity.Actor;
import com.harborvoice.platform.module.ApprovedModuleResolver;
import java.util.UUID;
import java.time.Instant;
import java.util.List;
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

    @Test void exposesOnlyTenantBoundOrderSummaries() {
        var actor = new Actor(UUID.randomUUID(), UUID.randomUUID(), Actor.Role.OWNER);
        ApprovedModuleResolver bindings = (business, module) -> new RestaurantBusinessModule();
        RestaurantOrderQuery orders = (business, limit) -> {
            assertThat(business).isEqualTo(actor.tenantId());
            assertThat(limit).isEqualTo(50);
            return List.of(new RestaurantOrderQuery.Summary(UUID.randomUUID(), UUID.randomUUID(), "USD", 1000,
                    OrderState.CONFIRMED, Instant.now()));
        };
        var controller = new RestaurantMenuController(new RestaurantMenuService(bindings), orders);

        assertThat(controller.orders(actor, 50)).hasSize(1).allMatch(summary -> summary.totalMinor() == 1000);
    }

    @Test void rejectsUnauthenticatedMenuAccess() {
        ApprovedModuleResolver unused = (business, module) -> new RestaurantBusinessModule();
        assertThatThrownBy(() -> new RestaurantMenuService(unused).approvedMenu(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("authenticated tenant required");
    }
}
