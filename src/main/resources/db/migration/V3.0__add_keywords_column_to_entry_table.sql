ALTER TABLE entry
    ADD COLUMN IF NOT EXISTS keywords VARCHAR(128)[];

CREATE INDEX IF NOT EXISTS entry_keywords_gin ON entry USING GIN (keywords);