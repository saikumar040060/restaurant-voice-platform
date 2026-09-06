CREATE TABLE action_outbox (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL REFERENCES action_requests(id),
    business_id UUID NOT NULL REFERENCES businesses(id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'DISPATCHED', 'UNKNOWN', 'RECONCILED')),
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (request_id),
    FOREIGN KEY (request_id, business_id) REFERENCES action_requests(id, business_id)
);
