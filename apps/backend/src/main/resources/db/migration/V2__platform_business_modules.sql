-- Additive platform foundation. Existing restaurant tables and IDs remain unchanged.
CREATE TABLE businesses (
    id UUID PRIMARY KEY REFERENCES tenants(id),
    business_type VARCHAR(40) NOT NULL CHECK (length(trim(business_type)) > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE business_modules (
    module_id VARCHAR(65) NOT NULL,
    version VARCHAR(40) NOT NULL,
    display_name VARCHAR(120) NOT NULL CHECK (length(trim(display_name)) > 0),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (module_id, version)
);

CREATE TABLE business_module_bindings (
    business_id UUID NOT NULL REFERENCES businesses(id),
    module_id VARCHAR(65) NOT NULL,
    module_version VARCHAR(40) NOT NULL,
    approval_state VARCHAR(20) NOT NULL CHECK (approval_state IN ('DRAFT', 'APPROVED', 'REVOKED')),
    PRIMARY KEY (business_id, module_id),
    FOREIGN KEY (module_id, module_version) REFERENCES business_modules(module_id, version)
);
