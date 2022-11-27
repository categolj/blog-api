SELECT UNNEST(tags) AS tag_name, COUNT(*) AS count
FROM entry
GROUP BY tag_name
ORDER BY tag_name;