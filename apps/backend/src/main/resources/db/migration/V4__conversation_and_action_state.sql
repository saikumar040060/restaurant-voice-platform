CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL REFERENCES businesses(id),
    state VARCHAR(24) NOT NULL CHECK (state IN ('CONNECTING', 'ACTIVE', 'TRANSFERRING', 'CALLBACK_PENDING', 'ENDED')),
    current_epoch BIGINT NOT NULL DEFAULT 0 CHECK (current_epoch >= 0),
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMPTZ
);
CREATE INDEX conversations_business_time ON conversations(business_id, started_at);

CREATE TABLE conversation_turns (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(id),
    business_id UUID NOT NULL REFERENCES businesses(id),
    sequence_no BIGINT NOT NULL CHECK (sequence_no >= 0),
    epoch BIGINT NOT NULL CHECK (epoch >= 0),
    speaker VARCHAR(24) NOT NULL CHECK (speaker IN ('CUSTOMER', 'AGENT', 'SYSTEM')),
    text TEXT NOT NULL CHECK (length(text) <= 16000),
    final_text BOOLEAN NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (conversation_id, sequence_no),
    FOREIGN KEY (conversation_id, business_id) REFERENCES conversations(id, business_id)
);

CREATE TABLE action_requests (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL REFERENCES businesses(id),
    conversation_id UUID NOT NULL REFERENCES conversations(id),
    tool_id VARCHAR(120) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROPOSED', 'AUTHORIZED', 'RUNNING', 'SUCCEEDED', 'FAILED', 'UNKNOWN')),
    arguments JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (business_id, idempotency_key),
    FOREIGN KEY (conversation_id, business_id) REFERENCES conversations(id, business_id)
);
