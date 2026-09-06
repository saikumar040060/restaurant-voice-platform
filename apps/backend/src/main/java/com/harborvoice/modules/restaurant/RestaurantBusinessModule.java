package com.harborvoice.modules.restaurant;

import com.harborvoice.platform.module.BusinessModule;
import com.harborvoice.platform.module.ModuleDescriptor;
import com.harborvoice.platform.module.ToolCapability;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public final class RestaurantBusinessModule implements BusinessModule {
    private static final ModuleDescriptor DESCRIPTOR =
            new ModuleDescriptor("restaurant", "1.0.0", "Restaurant ordering");
    private static final Set<String> INTENTS = Set.of("menu_question", "dish_explanation", "recommendation",
            "pickup_order", "availability", "human_transfer");

    @Override public ModuleDescriptor descriptor() { return DESCRIPTOR; }
    @Override public Set<String> supportedIntents() { return INTENTS; }
    @Override public Set<ToolCapability> tools() {
        return Set.of(new ToolCapability("restaurant.menu_lookup", false, false),
                new ToolCapability("restaurant.availability_lookup", false, false),
                new ToolCapability("restaurant.quote", true, false));
    }
}
