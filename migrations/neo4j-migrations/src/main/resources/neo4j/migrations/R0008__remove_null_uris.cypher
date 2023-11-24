// Cleanup existing data
MATCH (n:Class) WHERE n.uri = "null" SET n.uri = NULL;
