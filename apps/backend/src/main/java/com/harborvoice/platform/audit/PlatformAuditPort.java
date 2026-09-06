package com.harborvoice.platform.audit;

public interface PlatformAuditPort {
    void append(PlatformAuditEvent event);
}
