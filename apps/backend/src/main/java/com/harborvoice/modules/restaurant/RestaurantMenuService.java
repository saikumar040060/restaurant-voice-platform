package com.harborvoice.modules.restaurant;

import com.harborvoice.identity.Actor;
import com.harborvoice.platform.module.ApprovedModuleResolver;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** Tenant-scoped entry point for the reviewed restaurant module's menu view. */
@Service
public final class RestaurantMenuService {
    private final ApprovedModuleResolver bindings;

    public RestaurantMenuService(ApprovedModuleResolver bindings) {
        this.bindings = Objects.requireNonNull(bindings, "module bindings required");
    }

    public RestaurantMenuKnowledge approvedMenu(Actor actor) {
        if (actor == null || actor.tenantId() == null) throw new IllegalArgumentException("authenticated tenant required");
        bindings.requireApproved(actor.tenantId(), "restaurant");
        return HarborPizzaFixture.menu();
    }
}
