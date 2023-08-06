SELECT COUNT(e.entry_id) as count
FROM entry AS e
WHERE 1 = 1
/*[# th:if="${keywordsCount > 0}"]*/
/*[# th:each="i : ${#numbers.sequence(0, keywordsCount - 1)}"]*/
  AND e.keywords @> ARRAY[ /*[# mb:p="keywords[${i}]"]*/ 'JAVA' /*[/]*/ ]::character varying[]
/*[/]*/
/*[/]*/
/*[# th:if="${createdBy} != null"]*/
  AND e.created_by = /*[# mb:p="createdBy"]*/ 'Toshiaki Maki' /*[/]*/
/*[/]*/
/*[# th:if="${lastModifiedBy} != null"]*/
  AND e.last_modified_by = /*[# mb:p="lastModifiedBy"]*/ 'Toshiaki Maki' /*[/]*/
/*[/]*/
/*[# th:if="${not #lists.isEmpty(categories)}"]*/
/*[# th:each="category,stat : ${categories}"]*/
  AND e.categories/*[# th:utext="${'[' + stat.count + ']'}"]*/ [1] /*[/]*/ = /*[# mb:p="categories[${stat.index}]"]*/ 'Java' /*[/]*/
/*[/]*/
/*[/]*/
/*[# th:if="${tag} != null"]*/
  AND e.tags @> ARRAY[ /*[# mb:p="tag"]*/ 'Java' /*[/]*/ ]::character varying[]
/*[/]*/
/*[# th:if="${tenantId}"]*/
  AND e.tenant_id = /*[# mb:p="tenantId"]*/ '_'
/*[/]*/
/*[/]*/
/*[# th:unless="${tenantId}"][# th:utext="|  AND e.tenant_id = '_'|"][/][/]*/
