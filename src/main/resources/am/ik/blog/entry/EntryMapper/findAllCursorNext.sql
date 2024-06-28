SELECT
/*[# th:if="!${excludeEntryId}"]*/
    e.entry_id,
/*[/]*/
/*[# th:if="${excludeEntryId}"][# th:utext="|       NULL AS entry_id,|"][/][/]*/
/*[# th:if="!${excludeTitle}"]*/
    e.title,
/*[/]*/
/*[# th:if="${excludeTitle}"][# th:utext="|       NULL AS title,|"][/][/]*/
/*[# th:if="!${excludeContent}"]*/
    e.content,
/*[/]*/
/*[# th:if="${excludeContent}"][# th:utext="|       '' AS content,|"][/][/]*/
/*[# th:if="!${excludeCategories}"]*/
    COALESCE(e.categories, '{}') AS categories,
/*[/]*/
/*[# th:if="${excludeCategories}"][# th:utext="|       NULL AS categories,|"][/][/]*/
/*[# th:if="!${excludeTags}"]*/
    e.tags,
/*[/]*/
/*[# th:if="${excludeTags}"][# th:utext="|       NULL AS tags,|"][/][/]*/
/*[# th:if="!${excludeCreatedBy}"]*/
    e.created_by,
/*[/]*/
/*[# th:if="${excludeCreatedBy}"][# th:utext="|       NULL AS created_by,|"][/][/]*/
/*[# th:if="!${excludeCreatedDate}"]*/
    e.created_date,
/*[/]*/
/*[# th:if="${excludeCreatedDate}"][# th:utext="|       NULL AS created_date,|"][/][/]*/
/*[# th:if="!${excludeLastModifiedBy}"]*/
    e.last_modified_by,
/*[/]*/
/*[# th:if="${excludeLastModifiedBy}"][# th:utext="|       NULL AS last_modified_by,|"][/][/]*/
/*[# th:if="!${excludeLastModifiedDate}"]*/
    e.last_modified_date
/*[/]*/
/*[# th:if="${excludeLastModifiedDate}"][# th:utext="|       NULL AS last_modified_date|"][/][/]*/
FROM entry AS e
WHERE e.last_modified_date < COALESCE( /*[# mb:p="cursor"]*/ NULL /*[/]*/ , 'infinity'::timestamptz)
/*[# th:if="${keywordsCount > 0}"]*/
  /*+ BitmapScan(entry entry_content_trgm_idx) */
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