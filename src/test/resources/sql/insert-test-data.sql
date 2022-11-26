INSERT INTO entry (entry_id, title, content, categories, created_by, created_date,
                   last_modified_by, last_modified_date)
VALUES (99997, 'CategoLJ 4', 'This is a test data.', '{"x", "y"}', 'admin', '2017-03-31 00:00:00 UTC', 'making',
        '2017-03-31 00:00:00');

INSERT INTO tag (tag_name)
VALUES ('test1');
INSERT INTO tag (tag_name)
VALUES ('test2');
INSERT INTO tag (tag_name)
VALUES ('test3');

INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99997, 'test1');
INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99997, 'test3');

INSERT INTO entry (entry_id, title, content, categories, created_by, created_date, last_modified_by, last_modified_date)
VALUES (99998, 'Test!!', 'This is a test data.', '{"a", "b", "c"}', 'making', '2017-04-01 00:00:00 UTC', 'making',
        '2017-04-01 00:00:00');

INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99998, 'test1');
INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99998, 'test2');

INSERT INTO entry (entry_id, title, content, categories, created_by, created_date, last_modified_by, last_modified_date)
VALUES (99999, 'Hello World!!', 'Hello!', '{"x", "y", "z"}' ,'making', '2017-04-01 01:00:00 UTC', 'making',
        '2017-04-01 02:00:00');

INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99999, 'test1');
INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99999, 'test2');
INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99999, 'test3');