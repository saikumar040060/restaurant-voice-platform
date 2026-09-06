CREATE TABLE knowledge_items (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL REFERENCES businesses(id),
    location_id UUID,
    canonical_key VARCHAR(160) NOT NULL,
    content TEXT NOT NULL CHECK (length(content) BETWEEN 1 AND 32000),
    source_uri VARCHAR(2000),
    provenance VARCHAR(80) NOT NULL CHECK (length(trim(provenance)) > 0),
    version INTEGER NOT NULL CHECK (version > 0),
    approval_state VARCHAR(20) NOT NULL CHECK (approval_state IN ('DRAFT', 'APPROVED', 'REVOKED')),
    fresh_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (business_id, canonical_key, version),
    FOREIGN KEY (location_id, business_id) REFERENCES locations(id, tenant_id)
);
CREATE INDEX knowledge_approved_lookup ON knowledge_items(business_id, approval_state, fresh_until);
