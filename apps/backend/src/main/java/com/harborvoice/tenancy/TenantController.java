package com.harborvoice.tenancy;

import com.harborvoice.identity.Actor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TenantController {
    private final TenantService tenants;

    public TenantController(TenantService tenants) {
        this.tenants = tenants;
    }

    public record CreateRestaurant(@NotBlank @Size(max = 120) String name) { }

    @GetMapping("/api/v1/restaurants")
    List<TenantService.Restaurant> restaurants(@AuthenticationPrincipal Actor actor) {
        return tenants.restaurants(actor);
    }

    @PostMapping("/api/v1/restaurants")
    @ResponseStatus(HttpStatus.CREATED)
    TenantService.Restaurant create(@AuthenticationPrincipal Actor actor,
            @Valid @RequestBody CreateRestaurant input, HttpServletRequest request) {
        return tenants.createRestaurant(actor, input.name(), (UUID) request.getAttribute("correlationId"));
    }

    @GetMapping("/api/v1/locations/{id}")
    TenantService.Location location(@AuthenticationPrincipal Actor actor, @PathVariable UUID id) {
        return tenants.location(actor, id);
    }
}
