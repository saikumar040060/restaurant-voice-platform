package com.harborvoice.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final SecureRandom random = new SecureRandom();
    private final String dummyHash;

    public SessionService(JdbcTemplate jdbc, PasswordEncoder passwords) {
        this.jdbc = jdbc;
        this.passwords = passwords;
        this.dummyHash = passwords.encode(UUID.randomUUID().toString());
    }

    public record LoginResult(String token, Instant expiresAt) { }
    private record Account(Actor actor, String passwordHash, boolean enabled, boolean mfaRequired) { }

    @Transactional
    public Optional<LoginResult> login(String username, String password, UUID correlation) {
        var accounts = jdbc.query("SELECT * FROM employees WHERE username = ? FOR UPDATE",
                (rs, row) -> new Account(new Actor(rs.getObject("id", UUID.class),
                        rs.getObject("tenant_id", UUID.class), Actor.Role.valueOf(rs.getString("role"))),
                        rs.getString("password_hash"), rs.getBoolean("enabled"), rs.getBoolean("mfa_required")),
                username);
        Account account = accounts.isEmpty() ? null : accounts.getFirst();
        boolean matches = passwords.matches(password, account == null ? dummyHash : account.passwordHash());
        if (account == null) {
            return Optional.empty();
        }
        boolean allowed = matches && account.enabled() && !account.mfaRequired()
                && account.actor().role() != Actor.Role.SYSTEM && account.actor().role() != Actor.Role.SUPPORT;
        audit(account.actor(), "LOGIN", account.actor().employeeId(), allowed ? "SUCCESS" : "DENIED", correlation);
        if (!allowed) {
            return Optional.empty();
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiry = Instant.now().plusSeconds(900);
        jdbc.update("INSERT INTO auth_sessions(token_hash, tenant_id, employee_id, expires_at) VALUES (?, ?, ?, ?)",
                hash(token), account.actor().tenantId(), account.actor().employeeId(), Timestamp.from(expiry));
        return Optional.of(new LoginResult(token, expiry));
    }

    public Optional<Actor> authenticate(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            return Optional.empty();
        }
        return jdbc.query("""
                SELECT e.id, e.tenant_id, e.role FROM auth_sessions s
                JOIN employees e ON e.id = s.employee_id AND e.tenant_id = s.tenant_id
                WHERE s.token_hash = ? AND s.expires_at > CURRENT_TIMESTAMP
                  AND e.enabled AND NOT e.mfa_required AND e.role IN ('OWNER', 'MANAGER', 'EMPLOYEE')
                """, (rs, row) -> new Actor(rs.getObject("id", UUID.class), rs.getObject("tenant_id", UUID.class),
                        Actor.Role.valueOf(rs.getString("role"))), hash(token)).stream().findFirst();
    }

    @Transactional
    public void revoke(Actor actor, String token, UUID correlation) {
        jdbc.update("DELETE FROM auth_sessions WHERE token_hash = ? AND tenant_id = ? AND employee_id = ?",
                hash(token), actor.tenantId(), actor.employeeId());
        audit(actor, "LOGOUT", actor.employeeId(), "SUCCESS", correlation);
    }

    public void audit(Actor actor, String action, UUID target, String outcome, UUID correlation) {
        jdbc.update("""
                INSERT INTO audit_events(id, tenant_id, actor_id, action, target_id, outcome, correlation_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), actor.tenantId(), actor.employeeId(), action, target, outcome, correlation);
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }
}
