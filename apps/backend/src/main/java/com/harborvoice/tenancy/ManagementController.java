package com.harborvoice.tenancy;

import com.harborvoice.identity.Actor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManagementController {
    private final ManagementService management;

    public ManagementController(ManagementService management) {
        this.management = management;
    }

    public record NewLocation(@NotNull UUID restaurantId, @NotBlank @Size(max = 120) String name,
                              @NotBlank @Size(max = 80) String timezone) { }
    public record NewEmployee(@NotBlank @Size(max = 120) String username,
                              @NotBlank @Size(min = 12, max = 72) String password, @NotNull Actor.Role role) {
        @Override
        public String toString() { return "NewEmployee[redacted]"; }
    }
    public record AccessChange(@NotNull Actor.Role role, @NotNull Boolean enabled) { }
    public record Assignments(@NotNull @Size(max = 100) Set<@NotNull UUID> locationIds) { }

    @PostMapping("/api/v1/locations")
    @ResponseStatus(HttpStatus.CREATED)
    TenantService.Location location(@AuthenticationPrincipal Actor actor, @Valid @RequestBody NewLocation input,
            HttpServletRequest request) {
        return management.createLocation(actor, input.restaurantId(), input.name(), input.timezone(), correlation(request));
    }

    @GetMapping("/api/v1/employees")
    List<ManagementService.Employee> employees(@AuthenticationPrincipal Actor actor) {
        return management.employees(actor);
    }

    @PostMapping("/api/v1/employees")
    @ResponseStatus(HttpStatus.CREATED)
    ManagementService.Employee employee(@AuthenticationPrincipal Actor actor, @Valid @RequestBody NewEmployee input,
            HttpServletRequest request) {
        return management.createEmployee(actor, input.username(), input.password(), input.role(), correlation(request));
    }

    @PutMapping("/api/v1/employees/{id}/access")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void access(@AuthenticationPrincipal Actor actor, @PathVariable UUID id, @Valid @RequestBody AccessChange input,
            HttpServletRequest request) {
        management.updateEmployee(actor, id, input.role(), input.enabled(), correlation(request));
    }

    @PutMapping("/api/v1/employees/{id}/locations")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void locations(@AuthenticationPrincipal Actor actor, @PathVariable UUID id, @Valid @RequestBody Assignments input,
            HttpServletRequest request) {
        management.assignLocations(actor, id, input.locationIds(), correlation(request));
    }

    @PostMapping("/api/v1/employees/{id}/revoke-sessions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revoke(@AuthenticationPrincipal Actor actor, @PathVariable UUID id, HttpServletRequest request) {
        management.revokeSessions(actor, id, correlation(request));
    }

    @GetMapping("/api/v1/audit-events")
    List<ManagementService.AuditEvent> audit(@AuthenticationPrincipal Actor actor,
            @RequestParam(defaultValue = "50") int limit) {
        return management.audit(actor, limit);
    }

    private UUID correlation(HttpServletRequest request) {
        return (UUID) request.getAttribute("correlationId");
    }
}
