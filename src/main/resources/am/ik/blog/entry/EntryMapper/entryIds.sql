/*[# th:if="${keywordsCount > 0}"]*/
/*+ BitmapScan(e entry_content_trgm_idx) */
/*[/]*/
SELECT DISTINCT e.entry_id, e.last_modified_date
FROM entry AS e
WHERE 1 = 1
/*[# th:if="${keywordsCount > 0}"]*/
/*[# th:if="${keywordQuery}"][# th:utext="${keywordQuery}"][/][/]*/
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
  AND e.tags @> JSONB_BUILD_ARRAY(JSONB_BUILD_OBJECT('name', /*[# mb:p="tag"]*/'Java'/*[/]*/))
/*[/]*/
/*[# th:if="${tenantId}"]*/
  AND e.tenant_id = /*[# mb:p="tenantId"]*/ '_'
/*[/]*/
/*[/]*/
/*[# th:unless="${tenantId}"][# th:utext="|  AND e.tenant_id = '_'|"][/][/]*/
ORDER BY e.last_modified_date DESC
/*[# th:if="${limit}"]*/
LIMIT 20 OFFSET 0
/*[/]*/