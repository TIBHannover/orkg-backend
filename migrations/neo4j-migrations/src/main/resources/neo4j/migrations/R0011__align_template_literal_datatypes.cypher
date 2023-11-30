MATCH (n:PropertyShape)-[:RELATED {predicate_id: "sh:order"}]->(l:Literal) WHERE l.datatype <> "xsd:integer" SET l.datatype = "xsd:integer";
MATCH (n:PropertyShape)-[:RELATED {predicate_id: "sh:minCount"}]->(l:Literal) WHERE l.datatype <> "xsd:integer" SET l.datatype = "xsd:integer";
MATCH (n:PropertyShape)-[:RELATED {predicate_id: "sh:maxCount"}]->(l:Literal) WHERE l.datatype <> "xsd:integer" SET l.datatype = "xsd:integer";
