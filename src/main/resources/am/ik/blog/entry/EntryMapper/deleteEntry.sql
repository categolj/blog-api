DELETE
FROM entry AS e
WHERE e.entry_id = /*[# mb:p="entryId"]*/ 999 /*[/]*/
/*[# th:if="${tenantId}"]*/
  AND e.tenant_id = /*[# mb:p="tenantId"]*/ '_'
/*[/]*/
/*[/]*/
/*[# th:unless="${tenantId}"][# th:utext="|  AND e.tenant_id = '_'|"][/][/]*/