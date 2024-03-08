MATCH (n:Resource)
WITH n, apoc.coll.removeAll(labels(n), ["Resource", "Thing"]) AS labels
UNWIND labels AS label
MATCH (c:Class {id: label})
WHERE NOT EXISTS((n)-[:INSTANCE_OF]->(c))
CREATE (n)-[:INSTANCE_OF]->(c)
RETURN COUNT(n);
