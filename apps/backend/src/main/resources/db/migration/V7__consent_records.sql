CREATE TABLE consent_records (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL REFERENCES businesses(id),
    conversation_id UUID REFERENCES conversations(id),
    purpose VARCHAR(32) NOT NULL CHECK (purpose IN ('SERVICE', 'RECORDING', 'TRANSCRIPT', 'CALLBACK')),
    granted BOOLEAN NOT NULL,
    evidence_hash VARCHAR(64) NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX consent_business_conversation ON consent_records(business_id, conversation_id, purpose, recorded_at);
