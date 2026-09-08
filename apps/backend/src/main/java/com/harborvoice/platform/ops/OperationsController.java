package com.harborvoice.platform.ops;

import com.harborvoice.identity.Actor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Read-only, credential-free operations projection for authorized dashboard users. */
@RestController
public final class OperationsController {
    private final ProviderHealthRegistry providers;
    private final UsageBudget budget;

    public OperationsController(ProviderHealthRegistry providers, UsageBudget budget) {
        this.providers = providers;
        this.budget = budget;
    }

    @GetMapping("/api/v1/operations/snapshot")
    OperationsSnapshot snapshot(@AuthenticationPrincipal Actor actor) {
        if (actor == null || (actor.role() != Actor.Role.OWNER && actor.role() != Actor.Role.MANAGER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "operations access denied");
        }
        return OperationsSnapshot.from(providers, budget);
    }
}
