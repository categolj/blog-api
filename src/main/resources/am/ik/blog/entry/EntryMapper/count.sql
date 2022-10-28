SELECT COUNT(e.entry_id) as count
FROM entry AS e
         /*[# th:if="${tag} != null"]*/
         LEFT JOIN entry_tag AS et ON e.entry_id = et.entry_id
    /*[/]*/
    /*[# th:if="${categoryOrders} != null"]*/
         LEFT JOIN category AS c ON e.entry_id = c.entry_id
    /*[/]*/
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
  AND et.tag_name = /*[# mb:p="tag"]*/ 'Java' /*[/]*/
/*[/]*/
/*[# th:if="${not #lists.isEmpty(categoryOrders)}"]*/
/*[# th:each="categoryOrder : ${categoryOrders}"]*/
  AND c.category_name = /*[# mb:p="categoryOrder.category.name"]*/ 'Java' /*[/]*/
  AND c.category_order = /*[# mb:p="categoryOrder.categoryOrder"]*/ 1
/*[/]*/
/*[/]*/
/*[/]*/
