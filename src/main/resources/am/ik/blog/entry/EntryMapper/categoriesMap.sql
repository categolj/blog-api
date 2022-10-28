SELECT entry_id, category_name
FROM category
WHERE entry_id IN (/*[# mb:p="entryIds"]*/ 600 /*[/]*/)
ORDER BY category_order ASC