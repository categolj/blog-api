SELECT DISTINCT e.entry_id, e.last_modified_date
FROM entry AS e
WHERE 1 = 1
/*[# th:if="${keyword} != null"]*/
/*[# mb:bind="keywordPattern=|%${#likes.escapeWildcard(#strings.toLowerCase(keyword))}%|" /]*/
  AND lower(e.content) LIKE /*[# mb:p="keywordPattern"]*/ '%c%' /*[/]*/
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
  AND /*[# mb:p="tag"]*/ 'Java' /*[/]*/ = ANY(e.tags)
/*[/]*/
ORDER BY e.last_modified_date DESC
/*[# th:if="${limit}"]*/
LIMIT 20 OFFSET 0
/*[/]*/