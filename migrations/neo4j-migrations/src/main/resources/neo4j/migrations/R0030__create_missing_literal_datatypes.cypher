MATCH (n:Literal)
WHERE n.datatype IS NULL
SET n.datatype = "xsd:string"
RETURN COUNT(n);
