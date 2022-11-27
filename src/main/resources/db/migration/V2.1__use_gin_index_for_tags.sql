ALTER TABLE entry
    ADD COLUMN IF NOT EXISTS tags VARCHAR(255)[];

CREATE INDEX IF NOT EXISTS entry_tags_gin ON entry USING GIN (tags);

UPDATE entry
SET tags = grouped.tags
FROM (SELECT entry_id, ARRAY_AGG(tag_name ORDER BY tag_name) tags
      FROM entry_tag
      GROUP BY entry_id) AS grouped
WHERE entry.entry_id = grouped.entry_id;

DROP TABLE entry_tag;