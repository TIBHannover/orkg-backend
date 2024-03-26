MATCH (n:Thing)-[r:INSTANCE_OF]->(c:Class)
WITH n, c, COLLECT(r) AS rs
WITH n, c, TAIL(rs) AS tail
WHERE SIZE(tail) > 0
UNWIND tail AS r
DELETE r;
