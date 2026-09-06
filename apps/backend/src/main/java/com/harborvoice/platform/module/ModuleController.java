package com.harborvoice.platform.module;

import com.harborvoice.identity.Actor;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ModuleController {
    private final ModuleBindingService bindings;

    public ModuleController(ModuleBindingService bindings) {
        this.bindings = bindings;
    }

    public record ModuleView(String moduleId, String version, String displayName, Set<String> intents) { }

    @GetMapping("/api/v1/modules/{moduleId}")
    ModuleView approvedModule(@AuthenticationPrincipal Actor actor, @PathVariable String moduleId) {
        try {
            BusinessModule module = bindings.requireApproved(actor.tenantId(), moduleId);
            return new ModuleView(module.descriptor().moduleId(), module.descriptor().version(),
                    module.descriptor().displayName(), module.supportedIntents());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "module unavailable", ex);
        }
    }
}
