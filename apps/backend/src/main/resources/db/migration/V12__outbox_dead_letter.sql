ALTER TABLE action_outbox DROP CONSTRAINT action_outbox_status_check;
ALTER TABLE action_outbox ADD CONSTRAINT action_outbox_status_check
    CHECK (status IN ('PENDING', 'DISPATCHED', 'UNKNOWN', 'RECONCILED', 'DEAD_LETTER'));
