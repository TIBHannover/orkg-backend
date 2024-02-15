MATCH (:C14022)-[:RELATED {predicate_id: "P34"}]->(:C23007)-[:RELATED {predicate_id: "P45074"}]->(q:C20041)
WITH COLLECT(q) AS nodes
CALL apoc.refactor.rename.label("C20041", "C14024", nodes)
YIELD total
RETURN total;
