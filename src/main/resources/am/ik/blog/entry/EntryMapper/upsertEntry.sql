INSERT INTO entry (entry_id, tenant_id, title, content, categories, tags, keywords, created_by, created_date,
                   last_modified_by,
                   last_modified_date)
VALUES (/*[# mb:p="entryId"]*/ 999 /*[/]*/,
           /*[# th:if="${tenantId}"]*//*[# mb:p="tenantId"]*/ '_'/*[/]*//*[/]*/
           /*[# th:unless="${tenantId}"][# th:utext="| '_'|"][/][/]*/,
           /*[# mb:p="title"]*/ 'Hello World!' /*[/]*/,
           /*[# mb:p="content"]*/ 'This is a test post.' /*[/]*/,
                               STRING_TO_ARRAY(/*[# mb:p="categories"]*/ 'Java,Framework,Spring' /*[/]*/, ','),
           /*[# mb:p="tags"]*/ '[
    {
      "name": "Java"
    },
    {
      "name": "Programming"
    }
  ]' /*[/]*/ ::jsonb,
                               STRING_TO_ARRAY(/*[# mb:p="keywords"]*/ 'Hello,World' /*[/]*/, ','),
           /*[# mb:p="createdBy"]*/'Toshiaki Maki' /*[/]*/,
           /*[# mb:p="createdDate"]*/'2017-03-31 00:00:00' /*[/]*/,
           /*[# mb:p="lastModifiedBy"]*/'Toshiaki Maki' /*[/]*/,
           /*[# mb:p="lastModifiedDate"]*/'2017-03-31 00:00:00' /*[/]*/)
ON CONFLICT ON CONSTRAINT entry_pkey DO UPDATE SET title              = /*[# mb:p="title"]*/ 'Hello World!' /*[/]*/,
                                                   tenant_id          = /*[# th:if="${tenantId}"]*//*[# mb:p="tenantId"]*/ '_'/*[/]*//*[/]*//*[# th:unless="${tenantId}"][# th:utext="| '_'|"][/][/]*/,
                                                   content            = /*[# mb:p="content"]*/ 'This is a test post.' /*[/]*/,
                                                   categories         = STRING_TO_ARRAY(/*[# mb:p="categories"]*/
                                                           'Java,Framework,Spring' /*[/]*/, ','),
                                                   tags               = /*[# mb:p="tags"]*/ '[
                                                     {
                                                       "name": "Java"
                                                     },
                                                     {
                                                       "name": "Programming"
                                                     }
                                                   ]' /*[/]*/ ::jsonb,
                                                   keywords           = STRING_TO_ARRAY(/*[# mb:p="keywords"]*/ 'Hello,World' /*[/]*/, ','),
                                                   created_by         = /*[# mb:p="createdBy"]*/ 'Toshiaki Maki' /*[/]*/,
                                                   created_date       = /*[# mb:p="createdDate"]*/'2017-03-31 00:00:00' /*[/]*/,
                                                   last_modified_by   = /*[# mb:p="lastModifiedBy"]*/ 'Toshiaki Maki' /*[/]*/,
                                                   last_modified_date = /*[# mb:p="lastModifiedDate"]*/'2017-03-31 00:00:00' /*[/]*/
