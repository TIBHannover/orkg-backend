MATCH (parent:Class {id: "RosettaStoneStatement"})
MATCH (:RosettaNodeShape)-[:RELATED {predicate_id: "sh:targetClass"}]->(child:Class)
WHERE NOT EXISTS((child)-[:SUBCLASS_OF]->(parent))
WITH parent, child, REPLACE(toString(datetime()), "000000Z", "Z") AS created_at
CREATE (child)-[:SUBCLASS_OF {created_at: created_at, created_by: "00000000-0000-0000-0000-000000000000"}]->(parent)
