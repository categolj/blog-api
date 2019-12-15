INSERT INTO entry (entry_id, title, content, created_by, created_date, last_modified_by, last_modified_date)
VALUES (99997, 'CategoLJ 4', 'This is a test data.', 'admin', '2017-03-31 00:00:00', 'making',
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

INSERT INTO category (entry_id, category_name, category_order)
VALUES (99997, 'x', 0);
INSERT INTO category (entry_id, category_name, category_order)
VALUES (99997, 'y', 1);

INSERT INTO entry (entry_id, title, content, created_by, created_date, last_modified_by, last_modified_date)
VALUES (99998, 'Test!!', 'This is a test data.', 'making', '2017-04-01 00:00:00', 'making',
        '2017-04-01 00:00:00');

INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99998, 'test1');
INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99998, 'test2');

INSERT INTO category (entry_id, category_name, category_order)
VALUES (99998, 'a', 0);
INSERT INTO category (entry_id, category_name, category_order)
VALUES (99998, 'b', 1);
INSERT INTO category (entry_id, category_name, category_order)
VALUES (99998, 'c', 2);

INSERT INTO entry (entry_id, title, content, created_by, created_date, last_modified_by, last_modified_date)
VALUES (99999, 'Hello World!!', 'Hello!', 'making', '2017-04-01 01:00:00', 'making',
        '2017-04-01 02:00:00');

INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99999, 'test1');
INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99999, 'test2');
INSERT INTO entry_tag (entry_id, tag_name)
VALUES (99999, 'test3');

INSERT INTO category (entry_id, category_name, category_order)
VALUES (99999, 'x', 0);
INSERT INTO category (entry_id, category_name, category_order)
VALUES (99999, 'y', 1);
INSERT INTO category (entry_id, category_name, category_order)
VALUES (99999, 'z', 2);