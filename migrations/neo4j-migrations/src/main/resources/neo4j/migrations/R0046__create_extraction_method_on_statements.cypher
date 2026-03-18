MATCH (:Thing)-[r:RELATED]->(:Thing)
WHERE r.extraction_method IS NULL
SET r.extraction_method = "UNKNOWN";
