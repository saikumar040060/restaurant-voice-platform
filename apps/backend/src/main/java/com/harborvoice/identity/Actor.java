package com.harborvoice.identity;

import java.util.UUID;

public record Actor(UUID employeeId, UUID tenantId, Role role) {
    public enum Role { OWNER, MANAGER, EMPLOYEE, SUPPORT, SYSTEM }
}
