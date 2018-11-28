INSERT INTO entry (entry_id, title, content, created_by, created_date, last_modified_by, last_modified_date)
VALUES (99996, 'Empty Tag test', 'This is an entry which has no entry.', 'admin', '2016-03-31 00:00:00', 'making', '2016-03-31 00:00:00');

INSERT INTO category (entry_id, category_name, category_order)
VALUES (99996, 'empty', 0);
INSERT INTO category (entry_id, category_name, category_order)
VALUES (99996, 'tag', 1);