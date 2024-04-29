// Create version property for every Thing
MATCH (n:Thing) WHERE n.version IS NULL SET n.version = 0;
