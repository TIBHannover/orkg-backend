MATCH (:Comparison)-[r:RELATED {predicate_id: "P30"}]->(:ResearchField) SET r.predicate_id = "hasSubject";
