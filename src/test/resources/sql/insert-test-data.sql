INSERT INTO entry (entry_id, title, content, categories, tags, keywords, created_by, created_date,
                   last_modified_by, last_modified_date)
VALUES (99997, 'CategoLJ 4', 'This is a test data.', '{"x", "y"}', '[
  {
    "name": "test1",
    "version": null
  },
  {
    "name": "test3",
    "version": null
  }
]'::jsonb, '{"THIS", "TEST", "DATA"}', 'admin', '2017-03-31 00:00:00 UTC', 'making',
        '2017-03-31 00:00:00 UTC');

INSERT INTO entry (entry_id, title, content, categories, tags, keywords, created_by, created_date,
                   last_modified_by, last_modified_date)
VALUES (99998, 'Test!!', 'This is a test data.', '{"a", "b", "c"}', '[
  {
    "name": "test1",
    "version": null
  },
  {
    "name": "test2",
    "version": null
  }
]'::jsonb, '{"THIS", "TEST", "DATA"}', 'making', '2017-04-01 00:00:00 UTC', 'making',
        '2017-04-01 00:00:00 UTC');

INSERT INTO entry (entry_id, title, content, categories, tags, keywords, created_by, created_date,
                   last_modified_by, last_modified_date)
VALUES (99999, 'Hello World!!', 'Hello!', '{"x", "y", "z"}', '[
  {
    "name": "test1",
    "version": null
  },
  {
    "name": "test2",
    "version": null
  },
  {
    "name": "test3",
    "version": null
  }
]'::jsonb, '{"HELLO", "WORLD"}', 'making', '2017-04-01 01:00:00 UTC', 'making',
        '2017-04-01 02:00:00 UTC');