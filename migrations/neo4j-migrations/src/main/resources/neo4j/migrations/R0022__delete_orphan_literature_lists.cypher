MATCH (n:LiteratureList)
WHERE NOT EXISTS((n)--())
DELETE n;
