package com.harborvoice.tenancy;

import com.harborvoice.identity.Actor;
import com.harborvoice.identity.SessionService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TenantService {
    private final JdbcTemplate jdbc;
    private final SessionService sessions;

    public TenantService(JdbcTemplate jdbc, SessionService sessions) {
        this.jdbc = jdbc;
        this.sessions = sessions;
    }

    public record Restaurant(UUID id, String name) { }
    public record Location(UUID id, UUID restaurantId, String name, String timezone) { }

    public List<Restaurant> restaurants(Actor actor) {
        requireStaff(actor);
        return jdbc.query("""
                SELECT r.id, r.name FROM restaurants r WHERE r.tenant_id = ?
                AND (? = 'OWNER' OR EXISTS (
                    SELECT 1 FROM locations l JOIN employee_locations el
                    ON el.tenant_id = l.tenant_id AND el.location_id = l.id
                    WHERE l.tenant_id = r.tenant_id AND l.restaurant_id = r.id AND el.employee_id = ?))
                ORDER BY r.id
                """, (rs, row) -> new Restaurant(rs.getObject("id", UUID.class), rs.getString("name")),
                actor.tenantId(), actor.role().name(), actor.employeeId());
    }

    public Location location(Actor actor, UUID id) {
        requireStaff(actor);
        return jdbc.query("""
                SELECT l.* FROM locations l WHERE l.tenant_id = ? AND l.id = ?
                AND (? = 'OWNER' OR EXISTS (SELECT 1 FROM employee_locations el
                    WHERE el.tenant_id = l.tenant_id AND el.location_id = l.id AND el.employee_id = ?))
                """, (rs, row) -> new Location(rs.getObject("id", UUID.class),
                        rs.getObject("restaurant_id", UUID.class), rs.getString("name"), rs.getString("timezone")),
                actor.tenantId(), id, actor.role().name(), actor.employeeId()).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Restaurant createRestaurant(Actor actor, String name, UUID correlation) {
        requireOwner(actor);
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO restaurants(id, tenant_id, name) VALUES (?, ?, ?)", id, actor.tenantId(), name);
        sessions.audit(actor, "RESTAURANT_CREATED", id, "SUCCESS", correlation);
        return new Restaurant(id, name);
    }

    private void requireStaff(Actor actor) {
        if (actor == null || (actor.role() != Actor.Role.OWNER && actor.role() != Actor.Role.MANAGER
                && actor.role() != Actor.Role.EMPLOYEE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void requireOwner(Actor actor) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
