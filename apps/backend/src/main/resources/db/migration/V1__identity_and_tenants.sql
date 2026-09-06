CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL CHECK (length(trim(name)) > 0)
);
CREATE TABLE restaurants (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(120) NOT NULL CHECK (length(trim(name)) > 0),
    UNIQUE (tenant_id, id)
);
CREATE TABLE locations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL CHECK (length(trim(name)) > 0),
    timezone VARCHAR(80) NOT NULL,
    UNIQUE (tenant_id, id),
    FOREIGN KEY (tenant_id, restaurant_id) REFERENCES restaurants(tenant_id, id)
);
CREATE TABLE employees (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    username VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'MANAGER', 'EMPLOYEE', 'SUPPORT', 'SYSTEM')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    mfa_required BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, id)
);
CREATE TABLE employee_locations (
    tenant_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    location_id UUID NOT NULL,
    PRIMARY KEY (tenant_id, employee_id, location_id),
    FOREIGN KEY (tenant_id, employee_id) REFERENCES employees(tenant_id, id),
    FOREIGN KEY (tenant_id, location_id) REFERENCES locations(tenant_id, id)
);
CREATE TABLE auth_sessions (
    token_hash VARCHAR(64) PRIMARY KEY,
    tenant_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (tenant_id, employee_id) REFERENCES employees(tenant_id, id)
);
CREATE INDEX auth_sessions_expiry ON auth_sessions(expires_at);
CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    actor_id UUID NOT NULL,
    action VARCHAR(80) NOT NULL,
    target_id UUID NOT NULL,
    outcome VARCHAR(20) NOT NULL,
    correlation_id UUID NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id, actor_id) REFERENCES employees(tenant_id, id)
);
CREATE INDEX audit_events_tenant_time ON audit_events(tenant_id, occurred_at);
CREATE FUNCTION reject_audit_mutation() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'audit events are append-only';
END;
$$;
CREATE TRIGGER audit_append_only BEFORE UPDATE OR DELETE ON audit_events
FOR EACH ROW EXECUTE FUNCTION reject_audit_mutation();
CREATE TABLE login_limits (
    key_hash VARCHAR(64) PRIMARY KEY,
    attempts INTEGER NOT NULL,
    resets_at TIMESTAMPTZ NOT NULL
);
