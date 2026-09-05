package com.harborvoice.tenancy;

import com.harborvoice.identity.Actor;
import com.harborvoice.identity.PasswordRules;
import com.harborvoice.identity.SessionService;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ManagementService {
    private final JdbcTemplate jdbc;
    private final SessionService sessions;
    private final PasswordEncoder passwords;

    public ManagementService(JdbcTemplate jdbc, SessionService sessions, PasswordEncoder passwords) {
        this.jdbc = jdbc;
        this.sessions = sessions;
        this.passwords = passwords;
    }

    public record Employee(UUID id, String username, Actor.Role role, boolean enabled) { }
    public record AuditEvent(UUID id, UUID actorId, String action, UUID targetId,
                             String outcome, UUID correlationId, java.time.Instant occurredAt) { }

    @Transactional(readOnly = true)
    public List<Employee> employees(Actor actor) {
        owner(actor);
        return jdbc.query("SELECT id, username, role, enabled FROM employees WHERE tenant_id = ? ORDER BY id LIMIT 200",
                (rs, row) -> new Employee(rs.getObject("id", UUID.class), rs.getString("username"),
                        Actor.Role.valueOf(rs.getString("role")), rs.getBoolean("enabled")), actor.tenantId());
    }

    @Transactional
    public TenantService.Location createLocation(Actor actor, UUID restaurantId, String name,
            String timezone, UUID correlation) {
        owner(actor);
        if (!ZoneId.getAvailableZoneIds().contains(timezone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (!Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM restaurants WHERE tenant_id = ? AND id = ?)",
                Boolean.class, actor.tenantId(), restaurantId))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO locations(id, tenant_id, restaurant_id, name, timezone) VALUES (?, ?, ?, ?, ?)",
                id, actor.tenantId(), restaurantId, name, timezone);
        sessions.audit(actor, "LOCATION_CREATED", id, "SUCCESS", correlation);
        return new TenantService.Location(id, restaurantId, name, timezone);
    }

    @Transactional
    public Employee createEmployee(Actor actor, String username, String password, Actor.Role role, UUID correlation) {
        owner(actor);
        staffRole(role);
        PasswordRules.validate(password);
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO employees(id, tenant_id, username, password_hash, role) VALUES (?, ?, ?, ?, ?)",
                id, actor.tenantId(), username, passwords.encode(password), role.name());
        sessions.audit(actor, "EMPLOYEE_CREATED", id, "SUCCESS", correlation);
        return new Employee(id, username, role, true);
    }

    @Transactional
    public void updateEmployee(Actor actor, UUID id, Actor.Role role, boolean enabled, UUID correlation) {
        owner(actor);
        staffRole(role);
        mutableEmployee(actor, id);
        jdbc.update("UPDATE employees SET role = ?, enabled = ? WHERE tenant_id = ? AND id = ?",
                role.name(), enabled, actor.tenantId(), id);
        revoke(actor, id);
        sessions.audit(actor, "EMPLOYEE_ACCESS_CHANGED", id, "SUCCESS", correlation);
    }

    @Transactional
    public void assignLocations(Actor actor, UUID id, Set<UUID> ids, UUID correlation) {
        owner(actor);
        mutableEmployee(actor, id);
        for (UUID location : ids) {
            if (!Boolean.TRUE.equals(jdbc.queryForObject(
                    "SELECT EXISTS(SELECT 1 FROM locations WHERE tenant_id = ? AND id = ?)",
                    Boolean.class, actor.tenantId(), location))) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }
        jdbc.update("DELETE FROM employee_locations WHERE tenant_id = ? AND employee_id = ?", actor.tenantId(), id);
        for (UUID location : ids) {
            jdbc.update("INSERT INTO employee_locations VALUES (?, ?, ?)", actor.tenantId(), id, location);
        }
        sessions.audit(actor, "LOCATION_ASSIGNMENTS_CHANGED", id, "SUCCESS", correlation);
    }

    @Transactional
    public void revokeSessions(Actor actor, UUID id, UUID correlation) {
        owner(actor);
        var rows = jdbc.queryForList("SELECT id FROM employees WHERE tenant_id = ? AND id = ? FOR UPDATE",
                actor.tenantId(), id);
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        revoke(actor, id);
        sessions.audit(actor, "EMPLOYEE_SESSIONS_REVOKED", id, "SUCCESS", correlation);
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> audit(Actor actor, int limit) {
        owner(actor);
        return jdbc.query("""
                SELECT * FROM audit_events WHERE tenant_id = ? ORDER BY occurred_at DESC, id DESC LIMIT ?
                """, (rs, row) -> new AuditEvent(rs.getObject("id", UUID.class), rs.getObject("actor_id", UUID.class),
                        rs.getString("action"), rs.getObject("target_id", UUID.class), rs.getString("outcome"),
                        rs.getObject("correlation_id", UUID.class), rs.getTimestamp("occurred_at").toInstant()),
                actor.tenantId(), Math.max(1, Math.min(200, limit)));
    }

    private void revoke(Actor actor, UUID id) {
        jdbc.update("DELETE FROM auth_sessions WHERE tenant_id = ? AND employee_id = ?", actor.tenantId(), id);
    }

    private void mutableEmployee(Actor actor, UUID id) {
        var roles = jdbc.queryForList("SELECT role FROM employees WHERE tenant_id = ? AND id = ? FOR UPDATE",
                String.class, actor.tenantId(), id);
        if (roles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!roles.getFirst().equals("MANAGER") && !roles.getFirst().equals("EMPLOYEE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void owner(Actor actor) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void staffRole(Actor.Role role) {
        if (role != Actor.Role.MANAGER && role != Actor.Role.EMPLOYEE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
