MATCH (l:Literal)
WHERE l.extraction_method IS NULL
SET l.extraction_method = "UNKNOWN";
