CREATE TABLE business_profiles (
    business_id UUID NOT NULL REFERENCES businesses(id),
    version INTEGER NOT NULL CHECK (version > 0),
    locale VARCHAR(32) NOT NULL,
    timezone VARCHAR(80) NOT NULL,
    config JSONB NOT NULL,
    approval_state VARCHAR(20) NOT NULL CHECK (approval_state IN ('DRAFT', 'APPROVED', 'REVOKED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (business_id, version)
);
CREATE INDEX business_profiles_current ON business_profiles(business_id, approval_state, version DESC);
