UPDATE entry
SET tags = '[]'::jsonb
WHERE tags IS NULL;

ALTER TABLE entry
    ALTER COLUMN tags SET DEFAULT '[]'::jsonb;

ALTER TABLE entry
    ALTER COLUMN tags SET NOT NULL;