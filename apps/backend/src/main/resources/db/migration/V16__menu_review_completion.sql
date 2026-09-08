CREATE TABLE menu_review_completions (
    business_id UUID NOT NULL REFERENCES businesses(id),
    draft_revision VARCHAR(64) NOT NULL CHECK (draft_revision ~ '^[0-9a-f]{64}$'),
    decision_set_hash VARCHAR(64) NOT NULL CHECK (decision_set_hash ~ '^[0-9a-f]{64}$'),
    publication_state VARCHAR(16) NOT NULL DEFAULT 'UNPUBLISHED' CHECK (publication_state = 'UNPUBLISHED'),
    completed_by UUID NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (business_id, draft_revision),
    FOREIGN KEY (business_id, completed_by) REFERENCES employees(tenant_id, id)
);

CREATE FUNCTION reject_completed_menu_review_mutation() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM menu_review_completions
        WHERE business_id = NEW.business_id AND draft_revision = NEW.draft_revision
    ) THEN
        RAISE EXCEPTION 'completed menu review decisions are immutable';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER menu_review_decisions_after_completion
BEFORE INSERT OR UPDATE ON menu_review_decisions
FOR EACH ROW EXECUTE FUNCTION reject_completed_menu_review_mutation();
