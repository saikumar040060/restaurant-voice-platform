CREATE TABLE action_permits (
    request_id UUID PRIMARY KEY REFERENCES action_requests(id),
    business_id UUID NOT NULL REFERENCES businesses(id),
    tool_id VARCHAR(120) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id, business_id) REFERENCES action_requests(id, business_id)
);
CREATE INDEX action_permits_expiry ON action_permits(expires_at);
