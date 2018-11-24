DELETE
FROM entry
WHERE entry_id > 99990;

DELETE
FROM tag
WHERE tag_name LIKE 'test%';