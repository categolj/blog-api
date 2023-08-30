ALTER TABLE entry
    DROP COLUMN IF EXISTS tags;

ALTER TABLE entry
    ADD COLUMN IF NOT EXISTS tags JSONB;

UPDATE entry
SET tags = tags_json;

CREATE INDEX entry_tags_gin ON entry USING GIN (tags);