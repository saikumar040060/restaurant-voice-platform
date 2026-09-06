package com.harborvoice.platform.module;

import java.util.Set;
import org.springframework.stereotype.Component;

/** Fictional reference module used to prove platform wiring before a vertical is added. */
@Component
public final class ReferenceBusinessModule implements BusinessModule {
    private static final ModuleDescriptor DESCRIPTOR =
            new ModuleDescriptor("reference", "1.0.0", "Reference business");
    private static final Set<String> INTENTS = Set.of("faq", "callback_request", "human_transfer");

    @Override
    public ModuleDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Set<String> supportedIntents() {
        return INTENTS;
    }
}
