ALTER TABLE entry
    ADD COLUMN IF NOT EXISTS categories VARCHAR(255)[];

CREATE INDEX IF NOT EXISTS entry_categories_gin ON entry USING GIN (categories);

UPDATE entry
SET categories = grouped.categories
FROM (SELECT entry_id, ARRAY_AGG(category_name ORDER BY category_order) categories
      FROM category
      GROUP BY entry_id) AS grouped
WHERE entry.entry_id = grouped.entry_id;

DROP TABLE category;