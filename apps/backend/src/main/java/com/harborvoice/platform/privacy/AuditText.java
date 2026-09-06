package com.harborvoice.platform.privacy;

public record AuditText(String value) {
    public AuditText {
        if (value == null || value.length() > 4000) throw new IllegalArgumentException("invalid audit text");
        value = Redactor.redact(value);
    }
}
