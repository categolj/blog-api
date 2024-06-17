SELECT COUNT(e.entry_id) as count
FROM entry AS e
WHERE 1 = 1
/*[# th:if="${keywordsCount > 0}"]*/
/*[# th:each="i : ${#numbers.sequence(0, keywordsCount - 1)}"]*/
  AND e.content ILIKE /*[# mb:p="keywords[${i}]"]*/ '%Java%' /*[/]*/
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
  AND e.categories/*[# th:utext="${'[' + stat.count + ']'}"]*/ [1] /*[/]*/ = /*[# mb:p="categories[${stat.index}]"]*/
      'Java' /*[/]*/
/*[/]*/
/*[/]*/
/*[# th:if="${tag} != null"]*/
  AND tags @> JSONB_BUILD_ARRAY(JSONB_BUILD_OBJECT('name', /*[# mb:p="tag"]*/'Java'/*[/]*/))
/*[/]*/
/*[# th:if="${tenantId}"]*/
  AND e.tenant_id = /*[# mb:p="tenantId"]*/ '_'
/*[/]*/
/*[/]*/
/*[# th:unless="${tenantId}"][# th:utext="|  AND e.tenant_id = '_'|"][/][/]*/
