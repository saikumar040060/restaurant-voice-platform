package com.harborvoice.identity;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operator-only initial provisioning: deliberately has no HTTP endpoint. */
@Service
public class BootstrapService {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final SessionService sessions;

    public BootstrapService(JdbcTemplate jdbc, PasswordEncoder passwords, SessionService sessions) {
        this.jdbc = jdbc;
        this.passwords = passwords;
        this.sessions = sessions;
    }

    @Transactional
    public UUID initialize(String tenantName, String username, String password) {
        PasswordRules.validate(password);
        if (tenantName == null || tenantName.isBlank() || tenantName.length() > 120
                || username == null || username.isBlank() || username.length() > 120) {
            throw new IllegalArgumentException("Invalid bootstrap input");
        }
        jdbc.execute("SELECT pg_advisory_xact_lock(872619042)");
        if (jdbc.queryForObject("SELECT count(*) FROM tenants", Integer.class) != 0) {
            throw new IllegalStateException("Bootstrap is allowed only on an empty installation");
        }
        UUID tenantId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        jdbc.update("INSERT INTO tenants(id, name) VALUES (?, ?)", tenantId, tenantName);
        jdbc.update("INSERT INTO employees(id, tenant_id, username, password_hash, role) VALUES (?, ?, ?, ?, 'OWNER')",
                employeeId, tenantId, username, passwords.encode(password));
        sessions.audit(new Actor(employeeId, tenantId, Actor.Role.OWNER), "INSTALLATION_BOOTSTRAPPED",
                tenantId, "SUCCESS", UUID.randomUUID());
        return tenantId;
    }
}
