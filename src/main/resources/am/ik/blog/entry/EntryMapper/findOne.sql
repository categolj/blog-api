SELECT e.entry_id,
       e.title,
/*[# th:if="!${excludeContent}"]*/
       e.content,
/*[/]*/
       e.categories,
       e.created_by,
       e.created_date,
       e.last_modified_by,
       e.last_modified_date
FROM entry AS e
WHERE e.entry_id = /*[# mb:p="entryId"]*/ 500 /*[/]*/