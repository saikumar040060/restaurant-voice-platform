ALTER TABLE menu_review_decisions
    ADD COLUMN draft_revision VARCHAR(64) NOT NULL DEFAULT '1b93986febffafa634afcd83fb64d5dc06459333c202e713d6892bc4e0c13931',
    ADD CONSTRAINT menu_review_decisions_draft_revision_sha256
        CHECK (draft_revision ~ '^[0-9a-f]{64}$');
