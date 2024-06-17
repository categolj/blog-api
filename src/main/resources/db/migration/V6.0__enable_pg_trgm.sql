-- Enable pg_trgm extension
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Drop the keywords column
ALTER TABLE entry
    DROP COLUMN keywords;

-- Create a GIN index on the content column using pg_trgm
CREATE INDEX entry_content_trgm_idx ON entry USING gin (content gin_trgm_ops);