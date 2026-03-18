MATCH (c:Class)
WHERE c.extraction_method IS NULL
SET c.extraction_method = "UNKNOWN";
