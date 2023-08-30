ALTER TABLE entry
    ADD COLUMN IF NOT EXISTS tags_json JSONB;

CREATE INDEX entry_tags_json_gin ON entry USING GIN (tags_json);

UPDATE entry
SET tags_json = (SELECT JSON_AGG(JSONB_BUILD_OBJECT('name', tag))
                 FROM UNNEST(tags) AS tag)
WHERE tags IS NOT NULL
  AND tags_json IS NULL;