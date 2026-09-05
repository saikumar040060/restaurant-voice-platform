package com.harborvoice.identity;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginLimiter {
    private final JdbcTemplate jdbc;

    public LoginLimiter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean allow(String username, String source) {
        // Both counters are database-atomic and shared across application instances.
        // Never trust X-Forwarded-For before a trusted proxy is configured.
        int sourceCount = increment("source:" + source);
        if (sourceCount > 50) {
            return false;
        }
        return increment("account:" + username) <= 10;
    }

    private int increment(String key) {
        return jdbc.queryForObject("""
                INSERT INTO login_limits(key_hash, attempts, resets_at)
                VALUES (?, 1, CURRENT_TIMESTAMP + INTERVAL '5 minutes')
                ON CONFLICT (key_hash) DO UPDATE SET
                    attempts = CASE WHEN login_limits.resets_at <= CURRENT_TIMESTAMP
                        THEN 1 ELSE LEAST(login_limits.attempts + 1, 100000) END,
                    resets_at = CASE WHEN login_limits.resets_at <= CURRENT_TIMESTAMP
                        THEN CURRENT_TIMESTAMP + INTERVAL '5 minutes' ELSE login_limits.resets_at END
                RETURNING attempts
                """, Integer.class, SessionService.hash(key));
    }
}
