package com.harborvoice.platform.module;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Resolves only an approved module explicitly bound to the authenticated business. */
@Service
public class ModuleBindingService {
    private final JdbcTemplate jdbc;
    private final ModuleRegistry registry;

    public ModuleBindingService(JdbcTemplate jdbc, ModuleRegistry registry) {
        this.jdbc = jdbc;
        this.registry = registry;
    }

    public BusinessModule requireApproved(UUID businessId, String moduleId) {
        String version = jdbc.query("""
                SELECT b.module_version
                FROM business_module_bindings b
                JOIN business_modules m ON m.module_id = b.module_id AND m.version = b.module_version
                WHERE b.business_id = ? AND b.module_id = ?
                  AND b.approval_state = 'APPROVED' AND m.enabled
                """, rs -> rs.next() ? rs.getString(1) : null, businessId, moduleId);
        if (version == null) {
            throw new IllegalArgumentException("module is not approved for business");
        }
        BusinessModule module = registry.require(moduleId);
        if (!module.descriptor().version().equals(version)) {
            throw new IllegalStateException("database module version is not installed");
        }
        return module;
    }
}
