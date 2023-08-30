SELECT e.entry_id,
       e.title,
/*[# th:if="!${excludeContent}"]*/
       e.content,
/*[/]*/
/*[# th:if="${excludeContent}"][# th:utext="|       '' AS content,|"][/][/]*/
       COALESCE(e.categories, '{}')       AS categories,
       COALESCE(e.tags_json, '[]'::jsonb) AS tags,
       e.created_by,
       e.created_date,
       e.last_modified_by,
       e.last_modified_date
FROM entry AS e
WHERE e.entry_id = /*[# mb:p="entryId"]*/ 500 /*[/]*/
/*[# th:if="${tenantId}"]*/
  AND e.tenant_id = /*[# mb:p="tenantId"]*/ '_'
/*[/]*/
/*[/]*/
/*[# th:unless="${tenantId}"][# th:utext="|  AND e.tenant_id = '_'|"][/][/]*/