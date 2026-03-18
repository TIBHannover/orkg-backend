MATCH (p:Predicate)
WHERE p.extraction_method IS NULL
SET p.extraction_method = "UNKNOWN";
