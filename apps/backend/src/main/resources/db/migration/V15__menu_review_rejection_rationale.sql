ALTER TABLE menu_review_decisions
    ADD COLUMN rationale TEXT,
    ADD CONSTRAINT menu_review_decisions_rejection_rationale
        CHECK ((decision = 'REJECTED' AND rationale IS NOT NULL) OR (decision <> 'REJECTED' AND rationale IS NULL));
