MATCH (:C14022)-[:RELATED {predicate_id: "P34"}]->(:C23007)-[:RELATED {predicate_id: "P45074"}]->(q:C14024)
WITH COLLECT(q) AS nodes
CALL apoc.refactor.rename.label("C14024", "C20041", nodes)
YIELD total
RETURN total;
