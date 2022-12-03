SELECT DISTINCT e.categories
FROM entry AS e
WHERE 1 = 1
/*[# th:if="${tenantId}"]*/
  AND e.tenant_id = /*[# mb:p="tenantId"]*/ '_'
/*[/]*/
/*[/]*/
/*[# th:unless="${tenantId}"][# th:utext="|  AND e.tenant_id = '_'|"][/][/]*/
ORDER BY e.categories