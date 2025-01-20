MATCH (n:_ClassIdCounter) SET n.id = "ClassId", n:_IdCounter REMOVE n:_ClassIdCounter;
MATCH (n:_ListIdCounter) SET n.id = "ListId", n:_IdCounter REMOVE n:_ListIdCounter;
MATCH (n:_LiteralIdCounter) SET n.id = "LiteralId", n:_IdCounter REMOVE n:_LiteralIdCounter;
MATCH (n:_PredicateIdCounter) SET n.id = "PredicateId", n:_IdCounter REMOVE n:_PredicateIdCounter;
MATCH (n:_ResourceIdCounter) SET n.id = "ResourceId", n:_IdCounter REMOVE n:_ResourceIdCounter;
MATCH (n:_StatementIdCounter) SET n.id = "StatementId", n:_IdCounter REMOVE n:_StatementIdCounter;
