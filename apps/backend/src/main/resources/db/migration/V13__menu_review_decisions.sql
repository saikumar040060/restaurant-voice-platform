CREATE TABLE menu_review_decisions (
    business_id UUID NOT NULL REFERENCES businesses(id),
    item_index INTEGER NOT NULL CHECK (item_index > 0),
    decision VARCHAR(16) NOT NULL CHECK (decision IN ('APPROVED','CORRECTED','REJECTED')),
    correction TEXT,
    version INTEGER NOT NULL CHECK (version > 0),
    publication_state VARCHAR(16) NOT NULL DEFAULT 'UNPUBLISHED' CHECK (publication_state = 'UNPUBLISHED'),
    actor_id UUID NOT NULL REFERENCES employees(id),
    decided_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (business_id, item_index),
    CHECK ((decision = 'CORRECTED' AND correction IS NOT NULL) OR (decision <> 'CORRECTED' AND correction IS NULL))
);
