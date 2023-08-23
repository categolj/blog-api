SELECT e.entry_id,
              e.title,
/*[# th:if="!${excludeContent}"]*/
              e.content,
/*[/]*/
/*[# th:if="${excludeContent}"][# th:utext="|       '' AS content,|"][/][/]*/
              COALESCE(e.categories, '{}') AS categories,
              COALESCE(e.tags, '{}')       AS tags,
              e.created_by,
              e.created_date,
              e.last_modified_by,
              e.last_modified_date
       FROM entry AS e
       WHERE e.last_modified_date < COALESCE( /*[# mb:p="cursor"]*/ NULL /*[/]*/ , 'infinity'::timestamptz)
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
       ORDER BY e.last_modified_date DESC
/*[# th:if="${limit}"]*/
       LIMIT 20 OFFSET 0
/*[/]*/