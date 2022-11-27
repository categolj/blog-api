SELECT COUNT(e.entry_id) as count
FROM entry AS e
WHERE 1 = 1
/*[# th:if="${keyword} != null"]*/
/*[# mb:bind="keywordPattern=|%${#likes.escapeWildcard(#strings.toLowerCase(keyword))}%|" /]*/
  AND e.content LIKE /*[# mb:p="keywordPattern"]*/ '%c%' /*[/]*/
/*[/]*/
/*[# th:if="${createdBy} != null"]*/
  AND e.created_by = /*[# mb:p="createdBy"]*/ 'Toshiaki Maki' /*[/]*/
/*[/]*/
/*[# th:if="${lastModifiedBy} != null"]*/
  AND e.last_modified_by = /*[# mb:p="lastModifiedBy"]*/ 'Toshiaki Maki' /*[/]*/
/*[/]*/
/*[# th:if="${tag} != null"]*/
  AND /*[# mb:p="tag"]*/ 'Java' /*[/]*/ = ANY(e.tags)
/*[/]*/
