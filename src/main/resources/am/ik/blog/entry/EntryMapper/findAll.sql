SELECT e.entry_id,
       e.title,
/*[# th:if="!${excludeContent}"]*/
       e.content,
/*[/]*/
/*[# th:if="${excludeContent}"][# th:utext="|       '' AS content,|"][/][/]*/
       COALESCE(e.categories, '{}') AS categories,
       e.tags                       AS tags,
       e.created_by,
       e.created_date,
       e.last_modified_by,
       e.last_modified_date
FROM entry AS e
WHERE e.entry_id IN (/*[# mb:p="entryIds"]*/ 600 /*[/]*/)
/*[# th:if="${tenantId}"]*/
  AND e.tenant_id = /*[# mb:p="tenantId"]*/ '_'
/*[/]*/
/*[/]*/
/*[# th:unless="${tenantId}"][# th:utext="|  AND e.tenant_id = '_'|"][/][/]*/
ORDER BY e.last_modified_date DESC