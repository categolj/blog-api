INSERT INTO tag (tag_name)
VALUES (/*[# mb:p="tagName"]*/ 'Java' /*[/]*/)
ON CONFLICT ON CONSTRAINT tag_pkey DO UPDATE SET tag_name = /*[# mb:p="tagName"]*/ 'Java' /*[/]*/