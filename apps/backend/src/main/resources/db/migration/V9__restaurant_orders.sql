CREATE TABLE restaurant_orders (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL REFERENCES businesses(id),
    location_id UUID NOT NULL,
    quote_hash VARCHAR(64) NOT NULL,
    currency CHAR(3) NOT NULL,
    total_minor BIGINT NOT NULL CHECK (total_minor >= 0),
    state VARCHAR(20) NOT NULL CHECK (state IN ('DRAFT', 'CONFIRMED', 'SUBMITTING', 'ACCEPTED', 'UNKNOWN', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (location_id, business_id) REFERENCES locations(id, tenant_id)
);
CREATE INDEX restaurant_orders_scope_time ON restaurant_orders(business_id, location_id, created_at);
