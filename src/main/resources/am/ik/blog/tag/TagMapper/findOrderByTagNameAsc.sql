SELECT UNNEST(e.tags) AS tag_name, COUNT(*) AS count
FROM entry AS e
WHERE 1 = 1
/*[# th:if="${tenantId}"]*/
  AND e.tenant_id = /*[# mb:p="tenantId"]*/ '_'
/*[/]*/
/*[/]*/
/*[# th:unless="${tenantId}"][# th:utext="|  AND e.tenant_id = '_'|"][/][/]*/
GROUP BY tag_name
ORDER BY tag_name